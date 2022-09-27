package com.rullo.transportrouting.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application.
 */
@Getter
@Setter
@Configuration
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "transport-routing")
public class ApplicationProperties {

  private String routingDbHost;
  private int routingDbPort;
  private String routingDbUser;
  private String routingDbPassword;
  private String routingDbDatabase;
  private String routingDbSchema;
  private String routingDbTable;
  private String routingDbGeometryColumn;
  private double[][] routingBounds;
}
