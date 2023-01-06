package com.github.devraghav.bugtracker.issue.route;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class IssueRouteDefinition {

  @Bean
  public RouterFunction<ServerResponse> issueRoutes(
      IssueRouteDefinitionOpenAPIDocHelper docHelper, IssueRouteHandler issueRouteHandler) {
    Consumer<Builder> emptyOperationsConsumer = builder -> {};

    Supplier<RouterFunction<ServerResponse>> routerByIdSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", issueRouteHandler::get, docHelper::getIssueByIdOperationDoc)
                .PATCH("", issueRouteHandler::update, docHelper::updateIssueOperationDoc)
                .POST(
                    path("/files").and(accept(asMediaType(MULTIPART_FORM_DATA))),
                    issueRouteHandler::addAttachment,
                    docHelper::uploadFileOperationDoc)
                .POST("/comment", issueRouteHandler::addComment, docHelper::addCommentOperationDoc)
                .PUT(
                    "/comment/{commentId}",
                    issueRouteHandler::updateComment,
                    docHelper::updateCommentOperationDoc)
                .PATCH("/assignee", issueRouteHandler::assignee, docHelper::assigneeOperationDoc)
                .PATCH("/watch", issueRouteHandler::watch, docHelper::watcherOperationDoc)
                .DELETE("/watch", issueRouteHandler::unwatch, docHelper::removeWatcherOperationDoc)
                .PATCH("/resolve", issueRouteHandler::resolve, docHelper::resolveIssueOperationDoc)
                .build();

    Supplier<RouterFunction<ServerResponse>> routerFunctionSupplier =
        () ->
            SpringdocRouteBuilder.route()
                .GET("", issueRouteHandler::getAll, docHelper::getAllIssueOperationDoc)
                .POST(issueRouteHandler::create, docHelper::saveIssueOperationDoc)
                .nest(
                    path("/{id}").and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
                    routerByIdSupplier,
                    emptyOperationsConsumer)
                .build();

    return SpringdocRouteBuilder.route()
        .nest(
            path("/api/rest/v1/issue")
                .and(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON))),
            routerFunctionSupplier,
            emptyOperationsConsumer)
        .build();
  }
}
