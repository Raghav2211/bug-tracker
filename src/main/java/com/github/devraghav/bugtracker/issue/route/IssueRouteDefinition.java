package com.github.devraghav.bugtracker.issue.route;

import com.github.devraghav.bugtracker.issue.dto.*;
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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Slf4j
public class IssueRouteDefinition {

  @RouterOperations({
    @RouterOperation(
        path = "/api/rest/v1/issue/{id}",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.GET,
        beanMethod = "get",
        operation =
            @Operation(
                summary = "Get a issue by its id",
                operationId = "get",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Retrieve issue successfully",
                      content = @Content(schema = @Schema(implementation = Issue.class))),
                  @ApiResponse(
                      responseCode = "404",
                      description = "Issue not found",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                })),
    @RouterOperation(
        path = "/api/rest/v1/issue",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.POST,
        beanMethod = "create",
        operation =
            @Operation(
                summary = "Create issue",
                operationId = "create",
                responses = {
                  @ApiResponse(
                      responseCode = "201",
                      description = "Issue successfully created",
                      content = @Content(schema = @Schema(implementation = Issue.class))),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(schema = @Schema(implementation = IssueRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/issue",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.GET,
        beanMethod = "getAll",
        operation =
            @Operation(
                summary = "Get all issues",
                operationId = "getAll",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Retrieve all issues",
                      content =
                          @Content(
                              mediaType = APPLICATION_JSON_VALUE,
                              array = @ArraySchema(schema = @Schema(implementation = Issue.class))))
                })),
    @RouterOperation(
        path = "/api/rest/v1/issue/{id}/assign",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.PATCH,
        beanMethod = "assign",
        operation =
            @Operation(
                summary = "Assign issue",
                operationId = "assign",
                responses = {
                  @ApiResponse(
                      responseCode = "204",
                      description = "Assigned issue successfully",
                      content = @Content),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(
                                schema = @Schema(implementation = IssueAssignRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/issue/{id}/assign",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.DELETE,
        beanMethod = "unassign",
        operation =
            @Operation(
                summary = "Unassigned issue",
                operationId = "unassign",
                responses = {
                  @ApiResponse(
                      responseCode = "204",
                      description = "Unassigned issue successfully",
                      content = @Content),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(
                                schema = @Schema(implementation = IssueAssignRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/issue/{id}/watcher",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.PATCH,
        beanMethod = "addWatcher",
        operation =
            @Operation(
                summary = "Add watcher to issue",
                operationId = "addWatcher",
                responses = {
                  @ApiResponse(
                      responseCode = "204",
                      description = "Add watcher to issue successfully",
                      content = @Content),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(
                                schema = @Schema(implementation = IssueAssignRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/issue/{id}/watcher",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.DELETE,
        beanMethod = "addWatcher",
        operation =
            @Operation(
                summary = "Remove watcher to issue",
                operationId = "removeWatcher",
                responses = {
                  @ApiResponse(
                      responseCode = "204",
                      description = "Remove watcher from issue successfully",
                      content = @Content),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(
                                schema = @Schema(implementation = IssueAssignRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/issue/{id}/comment",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.POST,
        beanMethod = "addComment",
        operation =
            @Operation(
                summary = "Add comment in issue",
                operationId = "addComment",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Add comment in issue successfully",
                      content = @Content(schema = @Schema(implementation = IssueComment.class))),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(
                                schema = @Schema(implementation = IssueCommentRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/issue/{id}",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = IssueRouteHandler.class,
        method = RequestMethod.PATCH,
        beanMethod = "update",
        operation =
            @Operation(
                summary = "Update issue",
                operationId = "update",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Issue successfully updated",
                      content = @Content(schema = @Schema(implementation = Issue.class))),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = IssueErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                },
                requestBody =
                    @RequestBody(
                        content =
                            @Content(schema = @Schema(implementation = IssueUpdateRequest.class)))))
  })
  @Bean
  public RouterFunction<ServerResponse> issueRoutes(IssueRouteHandler issueRouteHandler) {

    return nest(
        path("/api/rest/v1/issue"),
        nest(path("/{id}"), routesById(issueRouteHandler))
            .andNest(
                accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)),
                basic(issueRouteHandler)));
  }

  private RouterFunction<ServerResponse> basic(IssueRouteHandler issueRouteHandler) {
    return route(method(HttpMethod.GET), issueRouteHandler::getAll)
        .andRoute(method(HttpMethod.POST), issueRouteHandler::create);
  }

  private RouterFunction<ServerResponse> routesById(IssueRouteHandler issueRouteHandler) {
    return route(method(HttpMethod.GET), issueRouteHandler::get)
        .andRoute(POST("/comment"), issueRouteHandler::addComment)
        .andRoute(PATCH("/assign"), issueRouteHandler::assign)
        .andRoute(DELETE("/assign"), issueRouteHandler::unassign)
        .andRoute(PATCH("/watcher"), issueRouteHandler::addWatcher)
        .andRoute(DELETE("/watcher"), issueRouteHandler::removeWatcher)
        .andRoute(DELETE("/done"), issueRouteHandler::done)
        .andRoute(method(HttpMethod.PATCH), issueRouteHandler::update);
  }
}
