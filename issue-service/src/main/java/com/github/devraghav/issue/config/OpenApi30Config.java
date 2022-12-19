package com.github.devraghav.issue.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    servers = @Server(url = "/", description = "Default Server URL"),
    info =
        @Info(
            title = "Issue Service",
            version = "1.0.0",
            description = "Issue service WebFlux Rest API"))
public class OpenApi30Config {

  @Bean
  public GroupedOpenApi issueOpenApi() {
    String paths[] = {"/api/rest/v1/issue/**"};
    return GroupedOpenApi.builder().group("issue-service").pathsToMatch(paths).build();
  }
}
