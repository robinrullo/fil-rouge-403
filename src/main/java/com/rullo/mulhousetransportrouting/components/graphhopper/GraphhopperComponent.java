package com.rullo.mulhousetransportrouting.components.graphhopper;

import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.rullo.mulhousetransportrouting.components.graphhopper.exception.CreateGraphhopperDirectoryError;
import com.rullo.mulhousetransportrouting.components.graphhopper.exception.DeleteGraphhopperDirectoryError;
import com.rullo.mulhousetransportrouting.config.ApplicationProperties;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * GraphHopper Component.
 */

@Component
@Slf4j
public class GraphhopperComponent {

  public static final CoordinateReferenceSystem CRS = DefaultGeographicCRS.WGS84;

  private static final String GRAPHHOPPER_DIR = "./build/tmp/graphhopper-db";
  private final ApplicationProperties appProp;
  private GraphHopper graphHopperApi;

  public GraphhopperComponent(ApplicationProperties appProp) {
    this.appProp = appProp;
  }

  /**
   * Create GraphHopper Bean for DI.
   *
   * @return GraphHopper instance.
   */
  @Bean
  public GraphHopper getGraphHopper() {
    if (graphHopperApi == null) {
      this.createHopperInstance();
    }
    return this.graphHopperApi;
  }

  /**
   * Create GraphHopper instance at application start.
   */
  public void createHopperInstance() {

    try {
      FileUtils.deleteDirectory(new File(GraphhopperComponent.GRAPHHOPPER_DIR));
    } catch (IOException e) {
      throw new DeleteGraphhopperDirectoryError(e);
    }

    if (new File(GraphhopperComponent.GRAPHHOPPER_DIR).mkdirs()) {
      log.info("Graphhopper directory created");
    } else {
      log.error("Graphhopper directory not created");
      throw new CreateGraphhopperDirectoryError("Failed to create Graphhopper temp directory");
    }

    Polygon geometry = new GeometryFactory().createPolygon(Arrays.stream(appProp.getRoutingBounds())
        .map(lngLat -> new Coordinate(lngLat[0], lngLat[1])).toArray(Coordinate[]::new));
    geometry.setSRID(4326);

    graphHopperApi = new GraphHopperPostgis();
    GraphHopperConfig graphHopperConfig = new GraphHopperConfig();
    graphHopperConfig.putObject("db.host", appProp.getRoutingDbHost());
    graphHopperConfig.putObject("db.port", appProp.getRoutingDbPort());
    graphHopperConfig.putObject("db.database", appProp.getRoutingDbDatabase());
    graphHopperConfig.putObject("db.schema", appProp.getRoutingDbSchema());
    graphHopperConfig.putObject("db.table", appProp.getRoutingDbTable());
    graphHopperConfig.putObject("db.user", appProp.getRoutingDbUser());
    graphHopperConfig.putObject("db.passwd", appProp.getRoutingDbPassword());
    graphHopperConfig.putObject("db.geomfilter", geometry);
    graphHopperConfig.putObject("db.geometry_column", appProp.getRoutingDbGeometryColumn());

    graphHopperConfig.putObject("graph.location", GraphhopperComponent.GRAPHHOPPER_DIR);
    graphHopperConfig.putObject("graph.flag_encoders", "foot,bike,car");
    graphHopperConfig.putObject("graph.dataaccess", "RAM_STORE");

    graphHopperConfig.setProfiles(Arrays.asList(
        new Profile("foot").setVehicle("foot").setWeighting("fastest").setTurnCosts(false),
        new Profile("bike").setVehicle("bike").setWeighting("fastest").setTurnCosts(false),
        new Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(false)));
    graphHopperApi.getCHPreparationHandler()
        .setCHProfiles(new CHProfile("car"), new CHProfile("bike"), new CHProfile("foot"));
    graphHopperApi.init(graphHopperConfig);
    graphHopperApi.importOrLoad();

    log.info("GraphHopper import done");
  }
}
