package com.github.devraghav.bugtracker.user.route.definition;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.core.fn.builders.securityrequirement.Builder.securityRequirementBuilder;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.devraghav.bugtracker.user.request.UserRequest;
import com.github.devraghav.bugtracker.user.response.UserResponse;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.core.fn.builders.apiresponse.Builder;
import org.springframework.stereotype.Component;

@Component
class UserOpenAPIDocHelper {
  // spotless:off
  void loginUserOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("login")
       .summary("Login user")
       .requestBody(requestBodyBuilder()
               .content(contentBuilder()
                       .schema(schemaBuilder().implementation(UserRequest.AuthRequest.class))))
       .response(responseBuilder().responseCode("200")
               .content(contentBuilder()
                       .schema(schemaBuilder().implementation(UserRequest.AuthResponse.class))))
       .response(responseBuilder()
               .responseCode("401")
               .description("Unauthorized user"))
       .build();
  }

  void signUpUserOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("signup")
        .summary("User Signup ")
        .requestBody(requestBodyBuilder()
                .content(contentBuilder()
                        .schema(schemaBuilder().implementation(UserRequest.CreateUser.class))))
        .response(saveUser201ResponseDoc())
        .response(savUser400ResponseDoc())
        .build();
  }

  void getAllUserOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("getAll")
        .summary("Get all users")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .response(getAll200ResponseDoc())
        .response(responseBuilder().responseCode("401").description("Unauthorized user"))
        .response(responseBuilder().responseCode("403").description("Forbidden"))
        .build();
  }

  void getUserByIdOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("get")
        .summary("Get a user by its id")
        .security(securityRequirementBuilder().name("bearerAuth"))
        .response(getUserById200ResponseDoc())
        .response(getUserById404ResponseDoc())
        .response(responseBuilder().responseCode("401").description("Unauthorized user"))
        .parameter(parameterBuilder()
                .in(ParameterIn.PATH)
                .name("id")
                .schema(schemaBuilder().type("string")))
        .build();
  }

  void updateUserOperationDoc(org.springdoc.core.fn.builders.operation.Builder ops) {
    ops.operationId("update")
            .summary("Update user by its id")
            .security(securityRequirementBuilder().name("bearerAuth"))
            .requestBody(requestBodyBuilder()
                    .content(contentBuilder()
                            .schema(schemaBuilder().implementation(UserRequest.UpdateUser.class))))
            .response(getUserById200ResponseDoc())
            .response(getUserById404ResponseDoc())
            .response(responseBuilder().responseCode("401").description("Unauthorized user"))
            .parameter(parameterBuilder()
                    .in(ParameterIn.PATH)
                    .name("id")
                    .schema(schemaBuilder().type("string")))
            .build();
  }

  private Builder saveUser201ResponseDoc() {
    return responseBuilder()
        .responseCode("201")
        .description("User successfully created")
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(UserResponse.User
                        .class)));
  }

  private Builder savUser400ResponseDoc() {
    return responseBuilder()
        .responseCode("400")
        .description("Bad Request")
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(UserResponse.Error.class)));
  }

  private Builder getAll200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve all users")
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .array(arraySchemaBuilder().schema(schemaBuilder().implementation(UserResponse.User.class))));
  }

  private Builder getUserById404ResponseDoc() {
    return responseBuilder()
        .responseCode("404")
        .description("User not found")
        .content(contentBuilder()
                .mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(UserResponse.Error.class)));
  }

  private Builder getUserById200ResponseDoc() {
    return responseBuilder()
        .responseCode("200")
        .description("Retrieve user successfully")
        .content(contentBuilder().mediaType(APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(UserResponse.User.class)));
  }
  // spotless:on
}
