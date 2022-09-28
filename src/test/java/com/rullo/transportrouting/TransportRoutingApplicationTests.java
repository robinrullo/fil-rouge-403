package com.rullo.transportrouting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class TransportRoutingApplicationTests {

  @Test
  void contextLoads(ApplicationContext context) {
    assertNotNull(context);
  }
}
