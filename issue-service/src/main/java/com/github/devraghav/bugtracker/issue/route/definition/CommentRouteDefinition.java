package com.github.devraghav.bugtracker.issue.route.definition;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import com.github.devraghav.bugtracker.issue.route.handler.CommentRouteHandler;
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
      CommentOpenAPIDocHelper docHelper, CommentRouteHandler commentRouteHandler) {
    Consumer<Builder> emptyOperationsConsumer = builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("/comment", commentRouteHandler::getAll, docHelper::getAllCommentOperationDoc)
                .GET(
                    path("/comments").and(contentType(TEXT_EVENT_STREAM)),
                    commentRouteHandler::subscribeCommentStream,
                    builder -> builder.operationId("stream").hidden(true).ignoreJsonView(true))
                .POST("/comment", commentRouteHandler::save, docHelper::addCommentOperationDoc)
                .PUT(
                    "/comment/{commentId}",
                    commentRouteHandler::update,
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
            commentRouteHandler::get,
            docHelper::getCommentByIdOperationDoc)
        .build();
  }
}
