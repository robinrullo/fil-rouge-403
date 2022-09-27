package com.rullo.transportrouting.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Open API Spec.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Open API Spec.
   *
   * @return OpenAPI
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().components(new Components()).info(
        new Info().title("Transport Routing API")
            .description("API for Transport Routing").version("v1.0"));
  }

  /**
   * Grouped Open API Spec.
   *
   * @return GroupedOpenApi
   */
  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder().group("com.rullo.transportrouting").pathsToMatch("/**")
        .build();
  }
}
