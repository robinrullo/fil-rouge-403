package com.rullo.transportrouting.controller;

import com.rullo.transportrouting.entity.dto.RouteRequest;
import com.rullo.transportrouting.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for routing.
 */

@RestController
@RequestMapping("routing")
public class RoutingController {

  private final RoutingService routingService;

  public RoutingController(RoutingService routingService) {
    this.routingService = routingService;
  }

  @PostMapping
  public Object getRouting(@RequestBody RouteRequest routeRequest) {
    return this.routingService.getRouting(routeRequest);
  }


  @PostMapping("update-data")
  public ResponseEntity<Void> updateData() {
    this.routingService.updateData();
    return ResponseEntity.noContent().build();
  }
}
