package com.rullo.mulhousetransportrouting.config;

import com.rullo.mulhousetransportrouting.entity.dto.PoiProjection;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Repository Rest Configuration.
 */

@Configuration
public class RestConfig implements RepositoryRestConfigurer {

  @Override
  public void configureRepositoryRestConfiguration(
      RepositoryRestConfiguration repositoryRestConfiguration, CorsRegistry cors) {
    repositoryRestConfiguration.getProjectionConfiguration()
        .addProjection(PoiProjection.class);
    cors.addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }

}
