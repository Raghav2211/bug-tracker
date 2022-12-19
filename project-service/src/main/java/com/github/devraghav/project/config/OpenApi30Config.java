package com.github.devraghav.project.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    servers = @Server(url = "/", description = "Default Server URL"),
    info =
        @Info(
            title = "Project Service",
            version = "1.0.0",
            description = "Project service WebFlux Rest API"))
public class OpenApi30Config {

  @Bean
  public GroupedOpenApi projectOpenApi() {
    String paths[] = {"/api/rest/v1/project/**"};
    return GroupedOpenApi.builder().group("project-service").pathsToMatch(paths).build();
  }
}
