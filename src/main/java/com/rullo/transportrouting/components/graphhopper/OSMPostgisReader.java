/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rullo.transportrouting.components.graphhopper;

import static com.graphhopper.util.DistanceCalcEarth.DIST_EARTH;
import static com.graphhopper.util.Helper.nf;
import static com.graphhopper.util.Helper.toLowerCase;

import com.graphhopper.coll.GHObjectIntHashMap;
import com.graphhopper.reader.OSMTurnRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.TurnCostParser;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.rullo.transportrouting.components.graphhopper.exception.DbFieldBadValueError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.DataStore;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Reads OSM data from Postgis and uses it in GraphHopper.
 *
 * @author Vikas Veshishth
 * @author Philip Welch
 * @author Mario Basa
 * @author Robin Boldt
 */
@Slf4j
public class OSMPostgisReader extends PostgisReader implements TurnCostParser.ExternalInternalMap {

  private static final int COORD_STATE_UNKNOWN = 0;
  private static final int COORD_STATE_PILLAR = -2;
  private static final int FIRST_NODE_ID = 1;
  private final String[] tagsToCopy;

  private final String tableName;

  private final String geometryColumn;

  private GHObjectIntHashMap<Coordinate> coordState = new GHObjectIntHashMap<>(1000, 0.7f);
  private static final DistanceCalc distCalc = DIST_EARTH;
  private int nextNodeId = FIRST_NODE_ID;
  protected long zeroCounter = 0;
  private final IntsRef tempRelFlags;

  private final HashMap<Long, WayNodes> wayNodesMap = new HashMap<>();
  private final HashMap<Integer, Long> edgeOsmIdMap = new HashMap<>();

  /**
   * Reader for OSM data from Postgis.
   *
   * @param ghStorage     The GraphHopper storage to write the data to.
   * @param postgisParams The Postgis parameters to use.
   */
  public OSMPostgisReader(GraphHopperStorage ghStorage, Map<String, Object> postgisParams) {
    super(ghStorage, postgisParams);

    String tmpTagsToCopy = (String) postgisParams.get("tags_to_copy");
    tableName = (String) postgisParams.get("table");
    geometryColumn = (String) postgisParams.get("geometry_column");

    if (tmpTagsToCopy == null || tmpTagsToCopy.isEmpty()) {
      this.tagsToCopy = new String[]{};
    } else {
      this.tagsToCopy = tmpTagsToCopy.split(",");
    }
    tempRelFlags = encodingManager.createRelationFlags();
    if (tempRelFlags.length != 2) {
      throw new IllegalArgumentException("Cannot use relation flags with != 2 integers");
    }
    tempRelFlags.ints[0] = (int) 0L;
    tempRelFlags.ints[1] = (int) 0L;
  }

  @Override
  void processJunctions() {
    DataStore dataStore = null;
    FeatureIterator<SimpleFeature> roads = null;
    int tmpJunctionCounter = 0;

    try {
      dataStore = openPostGisStore();
      roads = getFeatureIterator(dataStore, tableName, geometryColumn);

      HashSet<Coordinate> tmpSet = new HashSet<>();
      while (roads.hasNext()) {
        SimpleFeature road = roads.next();

        if (!acceptFeature(road)) {
          continue;
        }

        for (Coordinate[] points : getCoords(road)) {
          tmpSet.clear();
          for (int i = 0; i < points.length; i++) {
            Coordinate c = points[i];
            c = roundCoordinate(c);

            // don't add the same coord twice for the same edge - happens with bad geometry, i.e.
            // duplicate coords or a road which forms a circle (e.g. roundabout)
            if (tmpSet.contains(c)) {
              continue;
            }

            tmpSet.add(c);

            // skip if its already a node
            int state = coordState.get(c);
            if (state >= FIRST_NODE_ID) {
              continue;
            }

            if (i == 0 || i == points.length - 1 || state == COORD_STATE_PILLAR) {
              // turn into a node if it's the first or last
              // point, or already appeared in another edge
              int nodeId = nextNodeId++;
              coordState.put(c, nodeId);
              saveTowerPosition(nodeId, c);
            } else if (state == COORD_STATE_UNKNOWN) {
              // mark it as a pillar (which may get upgraded
              // to an edge later)
              coordState.put(c, COORD_STATE_PILLAR);
            }

            if (++tmpJunctionCounter % 100_000 == 0) {
              log.info("{} (junctions), junctionMap:{} {}", nf(tmpJunctionCounter),
                  nf(coordState.size()), Helper.getMemInfo());
            }
          }
        }
      }
    } finally {
      if (roads != null) {
        roads.close();
      }
      if (dataStore != null) {
        dataStore.dispose();
      }
    }

    if (nextNodeId == FIRST_NODE_ID) {
      throw new IllegalArgumentException("No data found for roads table " + tableName);
    }

    log.info("Number of junction points : {}", nextNodeId - FIRST_NODE_ID);
  }

  @Override
  void processRoads() {

    DataStore dataStore = null;
    FeatureIterator<SimpleFeature> roads = null;

    int tmpEdgeCounter = 0;

    try {
      dataStore = openPostGisStore();
      roads = getFeatureIterator(dataStore, tableName, geometryColumn);

      while (roads.hasNext()) {
        SimpleFeature road = roads.next();

        if (!acceptFeature(road)) {
          continue;
        }

        for (Coordinate[] points : getCoords(road)) {
          // Parse all points in the geometry, splitting into
          // individual GraphHopper edges
          // whenever we find a node in the list of points
          Coordinate startTowerPnt = null;
          List<Coordinate> pillars = new ArrayList<>();
          for (Coordinate point : points) {
            roundCoordinate(point);
            if (startTowerPnt == null) {
              startTowerPnt = point;
            } else {
              int state = coordState.get(point);
              if (state >= FIRST_NODE_ID) {
                int fromTowerNodeId = coordState.get(startTowerPnt);

                // get distance and estimated centre
                GHPoint estmCentre = new GHPoint(0.5 * (lat(startTowerPnt) + lat(point)),
                    0.5 * (lng(startTowerPnt) + lng(point)));
                PointList pillarNodes = new PointList(pillars.size(), false);

                for (Coordinate pillar : pillars) {
                  pillarNodes.add(lat(pillar), lng(pillar));
                }

                double distance = getWayLength(startTowerPnt, pillars, point);
                addEdge(fromTowerNodeId, state, road, distance, estmCentre, pillarNodes);
                startTowerPnt = point;
                pillars.clear();

                if (++tmpEdgeCounter % 1_000_000 == 0) {
                  log.info("{} (edges) {}", nf(tmpEdgeCounter), Helper.getMemInfo());
                }
              } else {
                pillars.add(point);
              }
            }
          }
        }

      }
    } finally {
      if (roads != null) {
        roads.close();
      }

      if (dataStore != null) {
        dataStore.dispose();
      }
    }
  }

  @Override
  void processRestrictions() {

    if (wayNodesMap.isEmpty()) {
      log.info("Ways Nodes data is empty");
      return;
    }

    DataStore dataStore = null;
    FeatureIterator<SimpleFeature> roads = null;

    try {
      dataStore = openPostGisStore();
      roads = getFeatureIterator(dataStore, tableName, geometryColumn);

      while (roads.hasNext()) {
        SimpleFeature road = roads.next();

        if (!acceptFeature(road)) {
          continue;
        }

        String restriction = (String) road.getAttribute("restriction");

        if (restriction == null) {
          continue;
        }

        OSMTurnRelation.Type type = OSMTurnRelation.Type.getRestrictionType(restriction);
        if (type == OSMTurnRelation.Type.UNSUPPORTED) {
          log.info("Unsupported: {}", restriction);
          continue;
        }

        long restrictionTo = Long.parseLong(road.getAttribute("restriction_to").toString());

        if (restrictionTo <= 0) {
          continue;
        }

        // read the OSM id, should never be null
        long restrictionFrom = getOSMId(road);

        WayNodes toWayNodes = wayNodesMap.get(restrictionTo);
        WayNodes fromWayNodes = wayNodesMap.get(restrictionFrom);

        if (toWayNodes == null || fromWayNodes == null) {
          continue;
        }

        int nodeId = 0;

        if (fromWayNodes.toNode() == toWayNodes.fromNode()) {
          nodeId = fromWayNodes.toNode();
        } else if (fromWayNodes.toNode() == toWayNodes.toNode()) {
          nodeId = fromWayNodes.toNode();
        } else if (fromWayNodes.fromNode() == toWayNodes.fromNode()) {
          nodeId = fromWayNodes.fromNode();
        } else if (fromWayNodes.fromNode() == toWayNodes.toNode()) {
          nodeId = fromWayNodes.fromNode();
        } else {
          continue;
        }

        OSMTurnRelation osmTurnRelation = new OSMTurnRelation(restrictionFrom, nodeId,
            restrictionTo, type);
        osmTurnRelation.setVehicleTypeRestricted("motorcar");

        log.info(osmTurnRelation.toString());

        encodingManager.handleTurnRelationTags(osmTurnRelation, this, graph);
      }
    } finally {
      if (roads != null) {
        roads.close();
      }

      if (dataStore != null) {
        dataStore.dispose();
      }
    }

  }

  @Override
  protected void finishReading() {
    this.coordState.clear();
    this.coordState = null;
    log.info("Finished reading. Zero Counter {} {}", nf(zeroCounter), Helper.getMemInfo());
  }

  protected double getWayLength(Coordinate start, List<Coordinate> pillars, Coordinate end) {
    double distance = 0;

    Coordinate previous = start;
    for (Coordinate point : pillars) {
      distance += distCalc.calcDist(lat(previous), lng(previous), lat(point), lng(point));
      previous = point;
    }
    distance += distCalc.calcDist(lat(previous), lng(previous), lat(end), lng(end));

    if (distance < 0.0001) {
      // As investigation shows often two paths should have crossed via one identical point
      // but end up in two very close points.
      zeroCounter++;
      distance = 0.0001;
    }

    if (Double.isNaN(distance)) {
      log.warn("Bug in OSM or GraphHopper. Illegal tower node distance {} reset to 1m, osm way {}",
          distance, distance);
      distance = 1;
    }

    return distance;
  }

  /**
   * Listener for when Edge is added.
   */
  public static interface EdgeAddedListener {

    void edgeAdded(ReaderWay way, EdgeIteratorState edge);
  }

  private void addEdge(int fromTower, int toTower, SimpleFeature road, double distance,
      GHPoint estmCentre, PointList pillarNodes) {
    EdgeIteratorState edge = graph.edge(fromTower, toTower);

    // read the OSM id, should never be null
    long id = getOSMId(road);

    // saving from.to nodes for restrictions and edgeId
    WayNodes wayNode = new WayNodes(fromTower, toTower);
    edgeOsmIdMap.put(edge.getEdge(), id);
    wayNodesMap.put(id, wayNode);

    // Make a temporary ReaderWay object with the properties we need so we
    // can use the enocding manager
    // We (hopefully don't need the node structure on here as we're only
    // calling the flag
    // encoders, which don't use this...
    ReaderWay way = new ReaderWay(id);

    way.setTag("estimated_distance", distance);
    way.setTag("estimated_center", estmCentre);

    // read name
    Object name = road.getAttribute("name");
    if (name != null) {
      way.setTag("name", name.toString());
    }

    // read the highway type
    Object type = road.getAttribute("fclass");
    if (type != null) {
      way.setTag("highway", type.toString());
    }

    // read maxspeed filtering for 0 which for Geofabrik shapefiles appears
    // to correspond to no tag
    Object maxSpeed = road.getAttribute("maxspeed");
    if (maxSpeed != null && !maxSpeed.toString().trim().equals("0")) {
      way.setTag("maxspeed", maxSpeed.toString());
    }

    for (String tag : tagsToCopy) {
      Object val = road.getAttribute(tag);
      if (val != null) {
        // for conditional fields i.e. "access_conditional" -> "access:conditional"
        if (tag.contains("_conditional")) {
          tag = tag.replace("_", ":");
        }

        way.setTag(tag, val.toString());
      }
    }

    // read oneway
    Object oneway = road.getAttribute("oneway");
    if (oneway != null) {
      // Geofabrik is using an odd convention for oneway field in
      // shapefile.
      // We map back to the standard convention so that tag can be dealt
      // with correctly by the flag encoder.
      String val = toLowerCase(oneway.toString().trim());
      switch (val) {
        case "b":
          // both ways
          val = "no";
          break;
        case "t":
          // one way against the direction of digitisation
          val = "-1";
          break;
        case "f":
          // one way Forward in the direction of digitisation
          val = "yes";
          break;
        case "yes", "no", "-1":
          // skip default OSM oneway tag values
          break;
        default:
          throw new DbFieldBadValueError(
              "Unrecognised value of oneway field \"%s\" found in road with OSM id %s".formatted(
                  val, id));
      }

      way.setTag("oneway", val);
    }

    // Process the flags using the encoders
    EncodingManager.AcceptWay acceptWay = new EncodingManager.AcceptWay();
    if (!encodingManager.acceptWay(way, acceptWay)) {
      return;
    }

    IntsRef edgeFlags = encodingManager.handleWayTags(way, acceptWay, tempRelFlags);
    if (edgeFlags.isEmpty()) {
      return;
    }

    edge.setDistance(distance);
    edge.setFlags(edgeFlags);
    edge.setWayGeometry(pillarNodes);
    encodingManager.applyWayTags(way, edge);
  }

  private long getOSMId(SimpleFeature road) {
    return Long.parseLong(road.getAttribute("osm_id").toString());
  }

  private Coordinate roundCoordinate(Coordinate c) {
    c.setX(Helper.round6(c.getX()));
    c.setY(Helper.round6(c.getY()));

    if (!Double.isNaN(c.getZ())) {
      c.setZ(Helper.round6(c.getZ()));
    }

    return c;
  }

  @Override
  public int getInternalNodeIdOfOsmNode(long nodeOsmId) {
    return (int) nodeOsmId;
  }

  @Override
  public long getOsmIdOfInternalEdge(int edgeId) {
    return edgeOsmIdMap.get(edgeId);
  }

}
