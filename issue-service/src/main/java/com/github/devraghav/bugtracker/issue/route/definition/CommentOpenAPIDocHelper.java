package com.github.devraghav.bugtracker.issue.route.definition;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.core.fn.builders.securityrequirement.Builder.securityRequirementBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.devraghav.bugtracker.issue.response.CommentResponse;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
class CommentOpenAPIDocHelper {

  // @spotless:off
  void getAllCommentOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAll")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Get all comment")
        .parameter(parameterBuilder()
                .in(ParameterIn.PATH)
                .name("issueId")
                .schema(schemaBuilder().type("string")))
        .response(getAll200ResponseDoc())
        .build();
  }

  void addCommentOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("addComment")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Add comment")
        .requestBody( requestBodyBuilder()
                .content(contentBuilder().schema(schemaBuilder().implementation(CreateComment.class))))
        .response(addOrUpdateComment200ResponseDoc())
        .response(badResponseDoc())
        .parameter(parameterBuilder()
                .in(ParameterIn.PATH)
                .name("issueId")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  void updateCommentOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("updateComment")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Update comment")
        .requestBody(requestBodyBuilder()
                .content(contentBuilder().schema(schemaBuilder().implementation(UpdateComment.class))))
        .response(addOrUpdateComment200ResponseDoc())
        .response(badResponseDoc())
        .parameter(parameterBuilder()
                .in(ParameterIn.PATH)
                .name("issueId")
                .schema(schemaBuilder().type("string")))
        .parameter(parameterBuilder()
                .in(ParameterIn.PATH)
                .name("commentId")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  void getCommentByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("get")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .summary("Get a comment by its id")
        .response(getCommentById200ResponseDoc())
        .response(comment404ResponseDoc())
        .parameter(parameterBuilder()
                .in(ParameterIn.PATH)
                .name("commentId")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder getAll200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve all comments")
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .array(arraySchemaBuilder().schema(schemaBuilder().implementation(CommentResponse.Comment.class))));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder addOrUpdateComment200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Add/Update comment in comment successfully")
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(CommentResponse.Comment.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder badResponseDoc() {
    return errorResponseDoc(HttpStatus.BAD_REQUEST, "Bad Request");
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder errorResponseDoc(
      HttpStatus httpStatus, String message) {
    return responseBuilder()
        .responseCode(String.valueOf(httpStatus.value()))
        .description(message)
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(CommentResponse.Error.class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder getCommentById200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve comment successfully")
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(CommentResponse.Comment .class)));
  }

  private org.springdoc.core.fn.builders.apiresponse.Builder comment404ResponseDoc() {
    return errorResponseDoc(HttpStatus.NOT_FOUND, "Comment not found");
  }

  record CreateComment(String content) {}

  record UpdateComment(String content) {}

  // @spotless:on
}
