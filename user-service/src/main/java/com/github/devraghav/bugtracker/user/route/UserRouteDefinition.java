package com.github.devraghav.bugtracker.user.route;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserRouteDefinition {

  @Bean
  public RouterFunction<ServerResponse> userRoutes(
      UserOpenAPIDocHelper docHelper, UserRouteHandler userRouteHandler) {
    Consumer<org.springdoc.core.fn.builders.operation.Builder> emptyOperationsConsumer =
        builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", userRouteHandler::getAll, docHelper::getAllUserOperationDoc)
                .POST("/login", userRouteHandler::login, docHelper::loginUserOperationDoc)
                .POST("/signup", userRouteHandler::create, docHelper::signUpUserOperationDoc)
                .GET("/{id}", userRouteHandler::get, docHelper::getUserByIdOperationDoc)
                .build();
    return SpringdocRouteBuilder.route()
        .nest(
            path("/api/rest/v1/user")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            routerFunctionSupplier,
            emptyOperationsConsumer)
        .build();
  }
}
