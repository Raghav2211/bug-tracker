package com.github.devraghav.bugtracker.project.route;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class ProjectRouteDefinition {

  @Bean
  public RouterFunction<ServerResponse> projectRoutes(
      ProjectOpenAPIDocHelper docHelper, ProjectRouteHandler projectRouteHandler) {
    Consumer<org.springdoc.core.fn.builders.operation.Builder> emptyOperationsConsumer =
        builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerByIdSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", projectRouteHandler::get, docHelper::getProjectByIdOperationDoc)
                .GET(
                    "/version",
                    projectRouteHandler::getAllProjectVersion,
                    docHelper::getAllVersionOperationDoc)
                .POST(
                    "/version", projectRouteHandler::addVersion, docHelper::saveVersionOperationDoc)
                .GET(
                    "/version/{versionId}",
                    projectRouteHandler::getProjectVersionById,
                    docHelper::getProjectVersionByIdOperationDoc)
                .build();

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", projectRouteHandler::getAll, docHelper::getAllProjectOperationDoc)
                .POST(projectRouteHandler::create, docHelper::saveProjectOperationDoc)
                .nest(
                    path("/{id}").and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
                    routerByIdSupplier,
                    emptyOperationsConsumer)
                .build();

    return SpringdocRouteBuilder.route()
        .nest(
            path("/api/rest/v1/project")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            routerFunctionSupplier,
            emptyOperationsConsumer)
        .build();
  }
}
