package com.github.devraghav.bugtracker.issue.route.definition;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.core.fn.builders.securityrequirement.Builder.securityRequirementBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class IssueOpenAPIDocHelper {

  // spotless:off
  void getAllIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAll")
        .security(securityRequirementBuilder().name("bearerAuth"))
       .summary("Get all issues")
       .parameter(parameterBuilder().in(ParameterIn.QUERY).name("page").schema(schemaBuilder().type("number")))
       .parameter(parameterBuilder().in(ParameterIn.QUERY).name("size").schema(schemaBuilder().type("number")))
       .response(getAll200ResponseDoc()).build();
  }

  void saveIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("create")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Create issue")
        .requestBody(requestBodyBuilder().content(contentBuilder().schema(schemaBuilder().implementation(IssueRequest.CreateIssue.class))))
        .response(saveIssue201ResponseDoc())
        .response(badResponseDoc())
        .build();
  }

  void updateIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("update")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Update issue")
        .requestBody(requestBodyBuilder().content(contentBuilder().schema(schemaBuilder().implementation(IssueRequest.UpdateIssue.class))))
        .response(updateIssue200ResponseDoc())
        .response(badResponseDoc())
        .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")))
        .build();
  }

  void getIssueByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("get")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Get a issue by its id")
        .response(getIssueById200ResponseDoc())
        .response(issue404ResponseDoc())
        .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")))
        .build();
  }

  void uploadFileOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("uploadFile")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Upload a file in issue")
        .requestBody(requestBodyBuilder().required(true).description("Upload issue attachments"))
        .response(responseBuilder().responseCode("200"))
        .response(badResponseDoc())
        .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")))
        .build();
  }

  void assigneeOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("assign")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Assign issue")
        .requestBody(requestBodyBuilder().content(contentBuilder().schema(schemaBuilder().implementation(Assign.class))))
        .response(responseBuilder().description("Assigned issue successfully").responseCode("204"))
        .response(badResponseDoc())
        .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")));
  }

  void unassignedOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("assign")
            .security(securityRequirementBuilder().name("bearerAuth"))
            .summary("Unassigned issue")
            .response(responseBuilder().description("Unassigned issue successfully").responseCode("204"))
            .response(badResponseDoc())
            .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")));
  }

  void watcherOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("addWatcher")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Add watcher to issue")
        .requestBody(requestBodyBuilder().content(contentBuilder().schema(schemaBuilder().implementation(Assign.class))))
        .response(responseBuilder().description("Add watcher to issue successfully").responseCode("204"))
        .response(badResponseDoc())
        .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")));
  }

  void removeWatcherOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("removeWatcher")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Remove watcher to issue")
        .requestBody(requestBodyBuilder().content(contentBuilder().schema(schemaBuilder().implementation(Assign.class))))
        .response(responseBuilder().description("Remove watcher from issue successfully").responseCode("204"))
        .response(badResponseDoc())
        .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")));
  }

  void resolveIssueOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("resolveIssue")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Resolve issue")
        .response(responseBuilder().description("Resolve issue successfully").responseCode("204"))
        .parameter(parameterBuilder().in(ParameterIn.PATH).name("id").schema(schemaBuilder().type("string")));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder issue404ResponseDoc() {
    return errorResponseDoc(HttpStatus.NOT_FOUND, "Issue not found");
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder getIssueById200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve issue successfully")
        .content(contentBuilder().mediaType(APPLICATION_JSON_VALUE).schema(schemaBuilder().implementation(IssueResponse.Issue
                .class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder saveIssue201ResponseDoc() {
    return responseBuilder()
        .responseCode("201")
        .description("Issue successfully created")
        .content(contentBuilder().mediaType(APPLICATION_JSON_VALUE).schema(schemaBuilder().implementation(IssueResponse.Issue.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder updateIssue200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Issue successfully updated")
        .content(contentBuilder().mediaType(APPLICATION_JSON_VALUE).schema(schemaBuilder().implementation(IssueResponse.Issue.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder badResponseDoc() {
    return errorResponseDoc(HttpStatus.BAD_REQUEST, "Bad Request");
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder getAll200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve all issues")
        .content(contentBuilder().mediaType(APPLICATION_JSON_VALUE)
                .array(arraySchemaBuilder().schema(schemaBuilder().implementation(IssueResponse.Issue.class))));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder errorResponseDoc(
      HttpStatus httpStatus, String message) {
    return responseBuilder()
        .responseCode(String.valueOf(httpStatus.value()))
        .description(message)
        .content(contentBuilder().mediaType(APPLICATION_JSON_VALUE).schema(schemaBuilder().implementation(IssueResponse.Error.class)));
  }
  // spotless:on
  record Assign(String user) {}
}
