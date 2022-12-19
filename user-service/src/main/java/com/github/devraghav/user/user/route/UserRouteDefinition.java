package com.github.devraghav.user.user.route;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import com.github.devraghav.user.user.dto.User;
import com.github.devraghav.user.user.dto.UserErrorResponse;
import com.github.devraghav.user.user.dto.UserRequest;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.RouterOperation;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserRouteDefinition {

  @Bean
  public RouterFunction<ServerResponse> userRoutes(UserRouteHandler userRouteHandler) {
    return SpringdocRouteBuilder.route()
        .nest(
            path("/api/rest/v1/user")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            () ->
                SpringdocRouteBuilder.route()
                    .GET("", userRouteHandler::getAll, this::getAllUserOperationDoc)
                    .POST(userRouteHandler::create, this::saveUserOperationDoc)
                    .GET("/{id}", userRouteHandler::get, this::getUserByIdOperationDoc)
                    .build(),
            ops -> {})
        .build();
  }

  private RouterOperation saveUserOperationDoc(
      org.springdoc.core.fn.builders.operation.Builder ops) {
    return ops.operationId("create")
        .summary("Create user")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder().schema(schemaBuilder().implementation(UserRequest.class))))
        .response(saveUser201ResponseDoc())
        .response(savUser400ResponseDoc())
        .build();
  }

  private RouterOperation getAllUserOperationDoc(
      org.springdoc.core.fn.builders.operation.Builder ops) {
    return ops.operationId("getAll")
        .summary("Get all users")
        .response(getAll200ResponseDoc())
        .build();
  }

  private RouterOperation getUserByIdOperationDoc(
      org.springdoc.core.fn.builders.operation.Builder ops) {
    return ops.operationId("get")
        .summary("Get a user by its id")
        .response(getUserById200ResponseDoc())
        .response(getUserById404ResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  private Builder saveUser201ResponseDoc() {
    return responseBuilder()
        .responseCode("201")
        .description("User successfully created")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(User.class)));
  }

  private Builder savUser400ResponseDoc() {
    return responseBuilder()
        .responseCode("400")
        .description("Bad Request")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(UserErrorResponse.class)));
  }

  private Builder getAll200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve all users")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .array(arraySchemaBuilder().schema(schemaBuilder().implementation(User.class))));
  }

  private Builder getUserById404ResponseDoc() {
    return responseBuilder()
        .responseCode("404")
        .description("User not found")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(UserErrorResponse.class)));
  }

  private Builder getUserById200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve user successfully")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(User.class)));
  }
}
