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

import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.TurnCostStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 * PostgisReader takes care of reading a PostGIS table and writing it to a road network graph.
 *
 * @author Vikas Veshishth
 * @author Philip Welch
 * @author Mario Basa
 * @author Robin Boldt
 */
@Slf4j
public abstract class PostgisReader {

  private final GraphHopperStorage graphStorage;
  private final NodeAccess nodeAccess;
  protected final TurnCostStorage turnCostStorage;
  protected final Graph graph;
  protected EncodingManager encodingManager;

  private final Map<String, Object> postgisParams;

  protected PostgisReader(GraphHopperStorage ghStorage, Map<String, Object> postgisParams) {

    this.graphStorage = ghStorage;
    this.graph = ghStorage;
    this.nodeAccess = graph.getNodeAccess();
    this.encodingManager = ghStorage.getEncodingManager();
    this.turnCostStorage = graph.getTurnCostStorage();
    this.postgisParams = postgisParams;
  }

  /**
   * Read the PostGIS table and write it to the graph.
   */
  public void readGraph() {
    graphStorage.create(1000);
    processJunctions();
    processRoads();
    processRestrictions();
    finishReading();
  }

  abstract void processJunctions();

  abstract void processRoads();

  abstract void processRestrictions();

  /**
   * This method will be called in the end to release the objects.
   */
  protected abstract void finishReading();

  protected FeatureIterator<SimpleFeature> getFeatureIterator(DataStore dataStore, String tableName,
      String geometryColumn) {

    if (dataStore == null) {
      throw new IllegalArgumentException("DataStore cannot be null for getFeatureIterator");
    }

    log.info("Getting the feature iterator for {}", tableName);

    try {
      FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(
          tableName);
      Filter filter = getFilter(source);

      if (this.postgisParams.get("geomfilter") instanceof Polygon polygon) {

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        Filter filter2 = ff.intersects(ff.property(geometryColumn), ff.literal(polygon));
        filter = ff.and(filter, filter2);
      }
      FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

      return collection.features();

    } catch (Exception e) {
      throw Utils.asUnchecked(e);
    }
  }

  /**
   * Filters can help a lot when you need to limit the results returned from PostGIS. A Filter can
   * be used similar to the WHERE clause in regular SQL statements. It's easy to filter geometries
   * that have a certain attributes, are in certain BBoxes, Polygons, etc. You can find a lot of
   * sample filters <a
   * href="https://github.com/geotools/geotools/blob/master/docs/src/main/java/org/geotools/main/FilterExamples.java">here</a>.
   * By default, all features are returned.
   */
  protected Filter getFilter(FeatureSource<SimpleFeatureType, SimpleFeature> source) {
    return Filter.INCLUDE;
  }

  protected DataStore openPostGisStore() {
    try {
      log.info("Opening DB connection to {} {}:{} to database {} schema {}",
          this.postgisParams.get("dbtype"), this.postgisParams.get("host"),
          this.postgisParams.get("port"), this.postgisParams.get("database"),
          this.postgisParams.get("schema"));
      DataStore ds = DataStoreFinder.getDataStore(this.postgisParams);
      if (ds == null) {
        throw new IllegalArgumentException("Error Connecting to Database ");
      }
      return ds;

    } catch (Exception e) {
      throw Utils.asUnchecked(e);
    }
  }

  /**
   * This method can be used to filter features. One way to use it is to filter for features withing
   * a certain BBox
   *
   * @return true if the feature should be accepted
   */
  protected boolean acceptFeature(SimpleFeature feature) {
    return true;
  }

  /**
   * Returns the coordinates of a feature.
   */
  protected List<Coordinate[]> getCoords(SimpleFeature feature) {
    ArrayList<Coordinate[]> ret = new ArrayList<>();
    if (feature == null) {
      return ret;
    }
    Object defaultGeometry = feature.getDefaultGeometry();
    if (defaultGeometry == null) {
      return ret;
    }

    if (defaultGeometry instanceof LineString coords) {
      ret.add(coords.getCoordinates());
    } else if (defaultGeometry instanceof MultiLineString coords) {
      int n = coords.getNumGeometries();
      for (int i = 0; i < n; i++) {
        ret.add(coords.getGeometryN(i).getCoordinates());
      }
    }

    return ret;
  }

  /*
   * Get longitude using the current long-lat order convention
   */
  protected double lng(Coordinate coordinate) {
    return coordinate.getOrdinate(0);
  }

  /*
   * Get latitude using the current long-lat order convention
   */
  protected double lat(Coordinate coordinate) {
    return coordinate.getOrdinate(1);
  }

  protected void saveTowerPosition(int nodeId, Coordinate point) {
    nodeAccess.setNode(nodeId, lat(point), lng(point));
  }
}
