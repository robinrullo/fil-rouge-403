package com.rullo.transportrouting.components.graphhopper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest()
class GraphhopperComponentTest {

  @Autowired
  private GraphhopperComponent graphhopperComponent;

  @Test
  void should_graphhopper_loaded_in_memory_graph() {
    assertTrue(graphhopperComponent.getGraphHopper().getFullyLoaded());
  }
}
