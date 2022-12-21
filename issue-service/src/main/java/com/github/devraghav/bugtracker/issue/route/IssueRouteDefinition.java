package com.github.devraghav.bugtracker.issue.route;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.IssueErrorResponse;
import com.github.devraghav.bugtracker.issue.dto.IssueRequest;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class IssueRouteDefinition {

  @Bean
  public RouterFunction<ServerResponse> issueRoutes(IssueRouteHandler issueRouteHandler) {
    Consumer<Builder> emptyOperationsConsumer = builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerByIdSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", issueRouteHandler::get, this::getIssueByIdOperationDoc)
                .PATCH("", issueRouteHandler::update, ops -> {})
                .POST("/comment", issueRouteHandler::addComment, ops -> {})
                .PATCH("/assign", issueRouteHandler::assign, ops -> {})
                .DELETE("/assign", issueRouteHandler::unassign, ops -> {})
                .PATCH("/watcher", issueRouteHandler::addWatcher, ops -> {})
                .DELETE("/watcher", issueRouteHandler::removeWatcher, ops -> {})
                .DELETE("/resolve", issueRouteHandler::done, ops -> {})
                .build();

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", issueRouteHandler::getAll, this::getAllIssueOperationDoc)
                .POST(issueRouteHandler::create, this::saveIssueOperationDoc)
                .nest(
                    path("/{id}").and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
                    routerByIdSupplier,
                    emptyOperationsConsumer)
                .build();

    return SpringdocRouteBuilder.route()
        .nest(
            path("/api/rest/v1/issue")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            routerFunctionSupplier,
            emptyOperationsConsumer)
        .build();
  }

  private void getAllIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAll").summary("Get all issues").response(getAll200ResponseDoc()).build();
  }

  private void saveIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("create")
        .summary("Create issue")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder().schema(schemaBuilder().implementation(IssueRequest.class))))
        .response(saveIssue201ResponseDoc())
        .response(savProject400ResponseDoc())
        .build();
  }

  private void getIssueByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("get")
        .summary("Get a project by its id")
        .response(getIssueById200ResponseDoc())
        .response(issue404ResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder issue404ResponseDoc() {
    return errorResponseDoc(HttpStatus.NOT_FOUND, "Issue not found");
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder getIssueById200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve issue successfully")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(Issue.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder saveIssue201ResponseDoc() {
    return responseBuilder()
        .responseCode("201")
        .description("Issue successfully created")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(Issue.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder savProject400ResponseDoc() {
    return errorResponseDoc(HttpStatus.BAD_REQUEST, "Bad Request");
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder getAll200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve all issues")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .array(arraySchemaBuilder().schema(schemaBuilder().implementation(Issue.class))));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder errorResponseDoc(
      HttpStatus httpStatus, String message) {
    return responseBuilder()
        .responseCode(String.valueOf(httpStatus.value()))
        .description(message)
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(IssueErrorResponse.class)));
  }
}
