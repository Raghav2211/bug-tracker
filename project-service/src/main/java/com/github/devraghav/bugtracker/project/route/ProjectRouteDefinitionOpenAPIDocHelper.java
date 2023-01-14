package com.github.devraghav.bugtracker.project.route;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.devraghav.bugtracker.project.dto.*;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class ProjectRouteDefinitionOpenAPIDocHelper {

  void getAllProjectOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAll").summary("Get all projects").response(getAll200ResponseDoc()).build();
  }

  void getAllVersionOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAllProjectVersion")
        .summary("Get all project versions")
        .response(getAllProjectVersion200ResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")));
  }

  void saveVersionOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("addVersion")
        .response(saveProjectVersion201ResponseDoc())
        .response(project404ResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(
                            schemaBuilder().implementation(ProjectRequest.CreateVersion.class))));
  }

  void getProjectByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("get")
        .summary("Get a project by its id")
        .response(getProjectById200ResponseDoc())
        .response(project404ResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  void getProjectVersionByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getVersion")
        .summary("Get a project version by its id")
        .response(getProjectVersionById200ResponseDoc())
        .response(getProjectVersionById404ResponseDoc())
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .parameter(
            parameterBuilder()
                .in(ParameterIn.PATH)
                .name("versionId")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  private Builder saveProject201ResponseDoc() {
    return responseBuilder()
        .responseCode("201")
        .description("Project successfully created")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(Project.class)));
  }

  private Builder badResponseDoc() {
    return errorResponseDoc(HttpStatus.BAD_REQUEST, "Bad Request");
  }

  private Builder getAll200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve all projects")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .array(arraySchemaBuilder().schema(schemaBuilder().implementation(Project.class))));
  }

  private Builder project404ResponseDoc() {
    return errorResponseDoc(HttpStatus.NOT_FOUND, "Project not found");
  }

  private Builder getProjectVersionById404ResponseDoc() {
    return errorResponseDoc(HttpStatus.NOT_FOUND, "Project version not found");
  }

  private Builder getProjectById200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve project successfully")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(Project.class)));
  }

  private Builder getProjectVersionById200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve project version successfully")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(Version.class)));
  }

  void saveProjectOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("create")
        .summary("Create project")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(schemaBuilder().implementation(ProjectRequest.Create.class))))
        .response(saveProject201ResponseDoc())
        .response(badResponseDoc())
        .build();
  }

  private Builder saveProjectVersion201ResponseDoc() {
    return responseBuilder()
        .responseCode("201")
        .description("Create project version successfully")
        .content(contentBuilder().schema(schemaBuilder().implementation(Version.class)));
  }

  private Builder getAllProjectVersion200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve all project versions")
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .array(
                    arraySchemaBuilder()
                        .arraySchema(schemaBuilder().implementation(Version.class))));
  }

  private Builder errorResponseDoc(HttpStatus httpStatus, String message) {
    return responseBuilder()
        .responseCode(String.valueOf(httpStatus.value()))
        .description(message)
        .content(
            contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(ProjectErrorResponse.class)));
  }
}
