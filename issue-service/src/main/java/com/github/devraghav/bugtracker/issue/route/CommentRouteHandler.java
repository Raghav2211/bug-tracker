package com.github.devraghav.bugtracker.issue.route;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.service.CommentCommandService;
import com.github.devraghav.bugtracker.issue.service.CommentQueryService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public record CommentRouteHandler(
    CommentCommandService commentCommandService, CommentQueryService commentQueryService) {

  public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
    var issueId = serverRequest.pathVariable("issueId");
    return commentQueryService
        .getComments(issueId)
        .collectList()
        .flatMap(CommentResponse::retrieve)
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(serverRequest, exception));
  }

  public Mono<ServerResponse> save(ServerRequest request) {
    var issueId = request.pathVariable("issueId");
    return request
        .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
        .map(body -> new CreateCommentRequest(body.get("user"), issueId, body.get("content")))
        .flatMap(commentCommandService::save)
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(CommentResponse.noBody(request))
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(request, exception))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> update(ServerRequest request) {
    var issueId = request.pathVariable("issueId");
    var commentId = request.pathVariable("commentId");
    return request
        .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
        .map(
            body ->
                new UpdateCommentRequest(body.get("user"), issueId, commentId, body.get("content")))
        .flatMap(commentCommandService::update)
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(CommentResponse.noBody(request))
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(request, exception))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    var commentId = request.pathVariable("commentId");
    return commentQueryService
        .getComment(commentId)
        .flatMap(project -> ServerResponse.ok().body(BodyInserters.fromValue(project)))
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(request, exception));
  }
}
