package com.github.devraghav.bugtracker.user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    servers = @Server(url = "/", description = "Default Server URL"),
    info =
        @Info(
            title = "User Service",
            version = "1.0.0",
            description = "User service WebFlux Rest API"))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer")
public class OpenApi30Config {

  @Bean
  public GroupedOpenApi userOpenApi() {
    String paths[] = {"/api/rest/v1/user/**"};
    return GroupedOpenApi.builder().group("user-service").pathsToMatch(paths).build();
  }
}
