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
class ProjectRouteDefinition {

  @Bean
  RouterFunction<ServerResponse> projectRoutes(
      ProjectOpenAPIDocHelper docHelper, RouteHandler routeHandler) {
    Consumer<org.springdoc.core.fn.builders.operation.Builder> emptyOperationsConsumer =
        builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerByIdSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", routeHandler::getProject, docHelper::getProjectByIdOperationDoc)
                .GET(
                    "/version",
                    routeHandler::getAllProjectVersion,
                    docHelper::getAllVersionOperationDoc)
                .POST(
                    "/version",
                    routeHandler::addVersionToProject,
                    docHelper::saveVersionOperationDoc)
                .GET(
                    "/version/{versionId}",
                    routeHandler::getProjectVersion,
                    docHelper::getProjectVersionByIdOperationDoc)
                .build();

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", routeHandler::getAllProjects, docHelper::getAllProjectOperationDoc)
                .POST(routeHandler::createProject, docHelper::saveProjectOperationDoc)
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
