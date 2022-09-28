package com.rullo.transportrouting.service;

import static org.junit.jupiter.api.Assertions.*;

import com.rullo.transportrouting.components.graphhopper.GraphhopperComponent;
import com.rullo.transportrouting.entity.dto.ItineraryResponse;
import com.rullo.transportrouting.entity.dto.RouteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest()
class RoutingServiceTest {

  @Autowired
  private RoutingService routingService;

  @Test
  void should_generate_an_itinerary() {

    var routeRequest = new RouteRequest("car",
        new double[][]{{7.3366355895996085, 47.76234916353148},
            {7.303504943847655, 47.75226580854138}, {7.321207523345947, 47.729697404198}});

    ItineraryResponse response = routingService.getRouting(routeRequest);
    assertEquals("LineString", response.itineraryFeature().getGeometryType());
    assertNotEquals(0, response.itineraryFeature().getCoordinates().length);
  }
}
