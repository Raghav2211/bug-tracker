package com.github.devraghav.user.user.route;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;

import com.github.devraghav.user.user.dto.User;
import com.github.devraghav.user.user.dto.UserErrorResponse;
import com.github.devraghav.user.user.dto.UserRequest;
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
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class UserRouteDefinition {
  @RouterOperations({
    @RouterOperation(
        path = "/api/rest/v1/user/{id}",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = UserRouteHandler.class,
        method = RequestMethod.GET,
        beanMethod = "get",
        operation =
            @Operation(
                summary = "Get a user by its id",
                operationId = "get",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Retrieve user successfully",
                      content = @Content(schema = @Schema(implementation = User.class))),
                  @ApiResponse(
                      responseCode = "404",
                      description = "User not found",
                      content = {
                        @Content(schema = @Schema(implementation = UserErrorResponse.class))
                      })
                },
                parameters = {
                  @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "string"))
                })),
    @RouterOperation(
        path = "/api/rest/v1/user",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = UserRouteHandler.class,
        method = RequestMethod.POST,
        beanMethod = "create",
        operation =
            @Operation(
                summary = "Create user",
                operationId = "create",
                responses = {
                  @ApiResponse(
                      responseCode = "201",
                      description = "User successfully created",
                      content = @Content(schema = @Schema(implementation = User.class))),
                  @ApiResponse(
                      responseCode = "400",
                      description = "Bad Request",
                      content = {
                        @Content(schema = @Schema(implementation = UserErrorResponse.class))
                      })
                },
                requestBody =
                    @RequestBody(
                        content = @Content(schema = @Schema(implementation = UserRequest.class))))),
    @RouterOperation(
        path = "/api/rest/v1/user",
        produces = {MediaType.APPLICATION_JSON_VALUE},
        beanClass = UserRouteHandler.class,
        method = RequestMethod.GET,
        beanMethod = "getAll",
        operation =
            @Operation(
                summary = "Get all users",
                operationId = "getAll",
                responses = {
                  @ApiResponse(
                      responseCode = "200",
                      description = "Retrieve all users",
                      content =
                          @Content(
                              mediaType = APPLICATION_JSON_VALUE,
                              array = @ArraySchema(schema = @Schema(implementation = User.class))))
                }))
  })
  @Bean
  public RouterFunction<ServerResponse> userRoutes(UserRouteHandler userRouteHandler) {
    return nest(
        path("/api/rest/v1/user"),
        nest(
            accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)),
            RouterFunctions.route(GET(""), userRouteHandler::getAll)
                .andRoute(method(HttpMethod.POST), userRouteHandler::create)
                .andNest(
                    path("/{id}"),
                    RouterFunctions.route(method(HttpMethod.GET), userRouteHandler::get))));
  }
}
