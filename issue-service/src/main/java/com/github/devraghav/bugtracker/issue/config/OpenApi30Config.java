package com.github.devraghav.bugtracker.issue.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
@Slf4j
@OpenAPIDefinition(
    servers = @Server(url = "/", description = "Default Server URL"),
    info =
        @Info(
            title = "Issue Service",
            version = "1.0.0",
            description = "Issue service WebFlux Rest API"))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer")
class OpenApi30Config {

  private Content uploadContent =
      new Content()
          .addMediaType(
              MediaType.MULTIPART_FORM_DATA_VALUE,
              new io.swagger.v3.oas.models.media.MediaType()
                  .addEncoding("file", new Encoding().style(Encoding.StyleEnum.FORM))
                  .schema(new ObjectSchema().addProperty("file", new FileSchema())));

  private Consumer<Operation> uploadOperationUpdateConsumer =
      operation -> {
        if (operation.getOperationId().startsWith("upload")) {
          RequestBody requestBody = operation.getRequestBody();
          requestBody.setContent(uploadContent);
        }
      };

  private final OpenApiCustomizer uploadOperationsCustomizer =
      openApi ->
          openApi.getPaths().values().stream()
              .flatMap(pathItem -> pathItem.readOperations().stream())
              .forEach(uploadOperationUpdateConsumer);

  @Bean
  GroupedOpenApi issueOpenApi() {
    String includePaths[] = {"/api/rest/**/issue/**"};
    String excludePaths[] = {
      "/api/rest/**/comment**", "/api/rest/**/comment/**",
    };
    return GroupedOpenApi.builder()
        .group("issue-service")
        .pathsToMatch(includePaths)
        .pathsToExclude(excludePaths)
        .addOpenApiCustomizer(uploadOperationsCustomizer)
        .build();
  }

  @Bean
  GroupedOpenApi commentOpenApi() {
    String includePaths[] = {"/api/rest/**/issue/{id}/comment/**", "/api/rest/**/comment/**"};
    String excludePaths[] = {"/api/rest/**/issue/{id}/comment/stream**"};
    return GroupedOpenApi.builder()
        .group("comment-service")
        .pathsToMatch(includePaths)
        .pathsToExclude(excludePaths)
        .addOpenApiCustomizer(uploadOperationsCustomizer)
        .build();
  }
}
