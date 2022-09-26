package com.rullo.mulhousetransportrouting.entity.dto;

import com.rullo.mulhousetransportrouting.entity.Poi;
import java.util.HashMap;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * POI Projection.
 */

@Projection(name = "poiGeoJSON", types = {Poi.class})
public interface PoiProjection {

  @Value("#{target.geometry}")
  Geometry getGeometry();

  @Value("#{{id: target.id, name: target.name, address: target.address, id: target.id}}")
  HashMap<String, Object> getProperties();
}
