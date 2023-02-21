package com.github.devraghav.bugtracker.project.route.defintition;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import com.github.devraghav.bugtracker.project.route.handler.ProjectRouteHandler;
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
      ProjectOpenAPIDocHelper docHelper, ProjectRouteHandler projectRouteHandler) {
    Consumer<org.springdoc.core.fn.builders.operation.Builder> emptyOperationsConsumer =
        builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerByIdSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .PATCH("", projectRouteHandler::updateProject, docHelper::updateProjectOperationDoc)
                .GET("", projectRouteHandler::getProject, docHelper::getProjectByIdOperationDoc)
                .GET(
                    "/version",
                    projectRouteHandler::getAllProjectVersion,
                    docHelper::getAllVersionOperationDoc)
                .POST(
                    "/version",
                    projectRouteHandler::addVersionToProject,
                    docHelper::saveVersionOperationDoc)
                .GET(
                    "/version/{versionId}",
                    projectRouteHandler::getProjectVersion,
                    docHelper::getProjectVersionByIdOperationDoc)
                .build();

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", projectRouteHandler::getAllProjects, docHelper::getAllProjectOperationDoc)
                .POST(projectRouteHandler::createProject, docHelper::saveProjectOperationDoc)
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
        .nest(
            path("/api/rest/internal/v1/project")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            () ->
                SpringdocRouteBuilder.route()
                    .GET(
                        "/{id}",
                        projectRouteHandler::getProject,
                        docHelper::getProjectByIdOperationDoc)
                    .build(),
            ops -> ops.hidden(true))
        .build();
  }
}
