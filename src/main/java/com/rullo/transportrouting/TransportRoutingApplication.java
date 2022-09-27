package com.rullo.transportrouting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main class for Transport Routing Application.
 *
 * @author Robin Rullo
 */

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class TransportRoutingApplication {

  public static void main(String[] args) {
    SpringApplication.run(TransportRoutingApplication.class, args);
  }

}
