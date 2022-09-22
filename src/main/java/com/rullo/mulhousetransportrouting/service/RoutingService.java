package com.rullo.mulhousetransportrouting.service;

import static com.graphhopper.util.Parameters.Routing.CALC_POINTS;
import static com.graphhopper.util.Parameters.Routing.INSTRUCTIONS;
import static com.graphhopper.util.Parameters.Routing.WAY_POINT_MAX_DISTANCE;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.jackson.ResponsePathSerializer;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;
import com.rullo.mulhousetransportrouting.components.graphhopper.GraphhopperComponent;
import com.rullo.mulhousetransportrouting.entity.dto.RouteRequest;
import com.rullo.mulhousetransportrouting.service.errors.CoordinateTransformationError;
import com.rullo.mulhousetransportrouting.service.errors.GraphhopperRoutingFailureError;
import java.util.Arrays;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for routing.
 */
@Service
@Slf4j
public class RoutingService {

  private final GraphHopper graphhopperApi;
  private final GraphhopperComponent graphhopperComponent;


  public RoutingService(GraphHopper graphhopperApi, GraphhopperComponent graphhopperComponent) {
    this.graphhopperApi = graphhopperApi;
    this.graphhopperComponent = graphhopperComponent;
  }

  /**
   * Get routing from RouteRequest.
   *
   * @param routeRequest request body.
   * @return routing response.
   */
  public Object getRouting(RouteRequest routeRequest) {
    StopWatch sw = new StopWatch().start();
    GHRequest request = new GHRequest();

    Arrays.stream(routeRequest.points()).map(pts -> {
      Point point = new GeometryFactory().createPoint(new Coordinate(pts[0], pts[1]));
      MathTransform transform;
      try {
        transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, GraphhopperComponent.CRS);
      } catch (FactoryException e) {
        throw new CoordinateTransformationError(e);
      }

      Point transformedPoint;
      try {
        transformedPoint = (Point) JTS.transform(point, transform);
      } catch (TransformException e) {
        throw new CoordinateTransformationError(e);
      }
      return new GHPoint(transformedPoint.getCentroid().getY(),
          transformedPoint.getCentroid().getX());


    }).forEach(request::addPoint);
    request.setLocale(Locale.FRANCE);
    request.setProfile(routeRequest.profile());
    request.getHints()
        .putObject(INSTRUCTIONS, true)
        //.putObject(CALC_POINTS, true)
        .putObject(WAY_POINT_MAX_DISTANCE, 1);

    GHResponse response = this.graphhopperApi.route(request);
    if (!response.hasErrors()) {
      log.info("Routing success: {}", response.getDebugInfo());
      return ResponsePathSerializer.jsonObject(response, true, true, true, false,
          sw.stop().getMillis());
    } else {
      log.error("Error while routing: {}", response.getErrors());
      throw new GraphhopperRoutingFailureError(response.getErrors());
    }
  }

  /**
   * Update GraphHopper data from PostgresSQL.
   */
  @Async
  public void updateData() {
    this.graphhopperComponent.createHopperInstance();
  }
}
