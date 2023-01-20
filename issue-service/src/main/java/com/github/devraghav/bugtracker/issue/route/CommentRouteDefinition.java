package com.github.devraghav.bugtracker.issue.route;

import static org.springframework.http.MediaType.*;
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
      CommentOpenAPIDocHelper docHelper, CommentRouteV1Handler commentRouteV1Handler) {
    Consumer<Builder> emptyOperationsConsumer = builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET(
                    "/comment", commentRouteV1Handler::getAll, docHelper::getAllCommentOperationDoc)
                .GET(
                    path("/comments").and(contentType(TEXT_EVENT_STREAM)),
                    commentRouteV1Handler::subscribeCommentStream,
                    builder -> builder.operationId("stream").hidden(true).ignoreJsonView(true))
                .POST("/comment", commentRouteV1Handler::save, docHelper::addCommentOperationDoc)
                .PUT(
                    "/comment/{commentId}",
                    commentRouteV1Handler::update,
                    docHelper::updateCommentOperationDoc)
                .build();

    return SpringdocRouteBuilder.route()
        .nest(
            path("/api/rest/v1/issue/{issueId}")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            routerFunctionSupplier,
            emptyOperationsConsumer)
        .GET(
            "/api/rest/v1/comment/{commentId}",
            commentRouteV1Handler::get,
            docHelper::getCommentByIdOperationDoc)
        .build();
  }
}
