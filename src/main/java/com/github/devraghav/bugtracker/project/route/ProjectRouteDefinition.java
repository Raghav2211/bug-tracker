package com.github.devraghav.bugtracker.project.route;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.github.devraghav.bugtracker.project.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class ProjectRouteDefinition {

  @RouterOperations({
    @RouterOperation(
        path = "/api/rest/v1/project/{id}",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = ProjectRouteHandler.class,
        method = RequestMethod.GET,
        beanMethod = "get",
        operation =
            @Operation(
                summary = "Get a project by its id",
                operationId = "get",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Retrieve project successfully",
                      content = @Content(schema = @Schema(implementation = Project.class))),
                  @ApiResponse(
                      responseCode = "404",
                      description = "Project not found",
                      content = {
                        @Content(schema = @Schema(implementation = ProjectErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                })),
    @RouterOperation(
        path = "/api/rest/v1/project",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = ProjectRouteHandler.class,
        method = RequestMethod.POST,
        beanMethod = "create",
        operation =
            @Operation(
                summary = "Create project",
                operationId = "create",
                responses = {
                  @ApiResponse(
                      responseCode = "201",
                      description = "Project successfully created",
                      content = @Content(schema = @Schema(implementation = Project.class))),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = ProjectErrorResponse.class))
                      })
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(schema = @Schema(implementation = ProjectRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/project",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = ProjectRouteHandler.class,
        method = RequestMethod.GET,
        beanMethod = "getAll",
        operation =
            @Operation(
                summary = "Get all projects",
                operationId = "getAll",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Retrieve all projects",
                      content =
                          @Content(
                              mediaType = APPLICATION_JSON_VALUE,
                              array =
                                  @ArraySchema(schema = @Schema(implementation = Project.class))))
                })),
    @RouterOperation(
        path = "/api/rest/v1/project/{id}/version",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = ProjectRouteHandler.class,
        method = RequestMethod.POST,
        beanMethod = "addVersion",
        operation =
            @Operation(
                summary = "Create a project version",
                operationId = "addVersion",
                responses = {
                  @ApiResponse(
                      responseCode = "201",
                      description = "Create project version successfully",
                      content = @Content(schema = @Schema(implementation = ProjectVersion.class))),
                  @ApiResponse(
                      responseCode = "404",
                      description = "Project not found",
                      content = {
                        @Content(schema = @Schema(implementation = ProjectErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(
                                schema = @Schema(implementation = ProjectVersionRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/project/{id}/version",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = ProjectRouteHandler.class,
        method = RequestMethod.GET,
        beanMethod = "getAllProjectVersion",
        operation =
            @Operation(
                summary = "Get all project versions",
                operationId = "getAllProjectVersion",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Retrieve all project versions",
                      content =
                          @Content(
                              mediaType = APPLICATION_JSON_VALUE,
                              array =
                                  @ArraySchema(
                                      schema = @Schema(implementation = ProjectVersion.class))))
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                }))
  })
  @Bean
  public RouterFunction<ServerResponse> projectRoutes(ProjectRouteHandler projectRouteHandler) {
    return nest(
        path("/api/rest/v1/project"),
        nest(path("/{id}"), routesById(projectRouteHandler))
            .andNest(
                accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)),
                basic(projectRouteHandler)));
  }

  private RouterFunction<ServerResponse> basic(ProjectRouteHandler projectRouteHandler) {
    return route(method(HttpMethod.GET), projectRouteHandler::getAll)
        .andRoute(method(HttpMethod.POST), projectRouteHandler::create);
  }

  private RouterFunction<ServerResponse> routesById(ProjectRouteHandler projectRouteHandler) {
    return route(GET("/version"), projectRouteHandler::getAllProjectVersion)
        .andRoute(POST("/version"), projectRouteHandler::addVersion)
        .andRoute(method(HttpMethod.GET), projectRouteHandler::get);
  }
}
