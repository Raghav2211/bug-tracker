package com.github.devraghav.bugtracker.user.route;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserErrorResponse;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springframework.stereotype.Component;

@Component
class UserRouteDefinitionOpenAPIDocHelper {

  void saveUserOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("create")
        .summary("Create user")
        .requestBody(
            requestBodyBuilder()
                .content(
                    contentBuilder()
                        .schema(schemaBuilder().implementation(CreateUserRequest.class))))
        .response(saveUser201ResponseDoc())
        .response(savUser400ResponseDoc())
        .build();
  }

  void getAllUserOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAll").summary("Get all users").response(getAll200ResponseDoc()).build();
  }

  void getUserByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("get")
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
