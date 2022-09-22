package com.rullo.mulhousetransportrouting;

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
public class MulhouseTransportRoutingApplication {

  public static void main(String[] args) {
    SpringApplication.run(MulhouseTransportRoutingApplication.class, args);
  }

}
