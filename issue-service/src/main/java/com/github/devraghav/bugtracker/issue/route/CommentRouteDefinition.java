package com.github.devraghav.bugtracker.issue.route;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class CommentRouteDefinition {

  @Bean
  public RouterFunction<ServerResponse> commentRoutes(
      IssueRouteDefinitionOpenAPIDocHelper docHelper, CommentRouteHandler commentRouteHandler) {
    Consumer<Builder> emptyOperationsConsumer = builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .POST("", commentRouteHandler::save, docHelper::addCommentOperationDoc)
                .PUT(
                    "/{commentId}",
                    commentRouteHandler::update,
                    docHelper::updateCommentOperationDoc)
                .build();

    return SpringdocRouteBuilder.route()
        .nest(
            path("/api/rest/v1/issue/{id}/comment")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            routerFunctionSupplier,
            emptyOperationsConsumer)
        .build();
  }
}
