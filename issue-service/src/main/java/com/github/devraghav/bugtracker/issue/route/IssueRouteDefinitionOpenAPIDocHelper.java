package com.github.devraghav.bugtracker.issue.route;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.devraghav.bugtracker.issue.dto.*;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class IssueRouteDefinitionOpenAPIDocHelper {

  void getAllIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAll").summary("Get all issues").response(getAll200ResponseDoc()).build();
  }

  void saveIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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

  void updateIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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

  void getIssueByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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

  void addCommentOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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

  void assigneeOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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

  void watcherOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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

  void removeWatcherOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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

  void resolveIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
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
