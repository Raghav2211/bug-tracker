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

import com.github.devraghav.bugtracker.issue.dto.*;
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
                .PATCH("", issueRouteHandler::update, this::updateIssueOperationDoc)
                .POST("/comment", issueRouteHandler::addComment, this::addCommentOperationDoc)
                .PATCH("/assign", issueRouteHandler::assign, this::addAssigneeOperationDoc)
                .DELETE("/assign", issueRouteHandler::unassign, this::removeAssigneeOperationDoc)
                .PATCH("/watcher", issueRouteHandler::addWatcher, this::addWatcherOperationDoc)
                .DELETE(
                    "/watcher", issueRouteHandler::removeWatcher, this::removeWatcherOperationDoc)
                .PATCH("/resolve", issueRouteHandler::done, this::resolveIssueOperationDoc)
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
        .response(badResponseDoc())
        .build();
  }

  private void updateIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("update")
        .summary("Update issue")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(schemaBuilder().implementation(IssueUpdateRequest.class))))
        .response(updateIssue200ResponseDoc())
        .response(badResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  private void getIssueByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("get")
        .summary("Get a issue by its id")
        .response(getIssueById200ResponseDoc())
        .response(issue404ResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  private void addCommentOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("addComment")
        .summary("Add comment in issue")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(schemaBuilder().implementation(IssueCommentRequest.class))))
        .response(addComment200ResponseDoc())
        .response(badResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  private void addAssigneeOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("assign")
        .summary("Assign issue")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(schemaBuilder().implementation(IssueAssignRequest.class))))
        .response(responseBuilder().description("Assigned issue successfully").responseCode("204"))
        .response(badResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")));
  }

  private void removeAssigneeOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("unassigned")
        .summary("Unassigned issue")
        .response(
            responseBuilder().description("Unassigned issue successfully").responseCode("204"))
        .response(badResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")));
  }

  private void addWatcherOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("addWatcher")
        .summary("Add watcher to issue")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(schemaBuilder().implementation(IssueAssignRequest.class))))
        .response(
            responseBuilder().description("Add watcher to issue successfully").responseCode("204"))
        .response(badResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")));
  }

  private void removeWatcherOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("removeWatcher")
        .summary("Remove watcher to issue")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(schemaBuilder().implementation(IssueAssignRequest.class))))
        .response(
            responseBuilder()
                .description("Remove watcher from issue successfully")
                .responseCode("204"))
        .response(badResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")));
  }

  private void resolveIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("resolveIssue")
        .summary("Resolve issue")
        .response(responseBuilder().description("Resolve issue successfully").responseCode("204"))
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")));
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

  private org.springdoc.core.fn.builders.apiresponse.Builder updateIssue200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Issue successfully updated")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(Issue.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder addComment200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Add comment in issue successfully")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(IssueComment.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder badResponseDoc() {
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
