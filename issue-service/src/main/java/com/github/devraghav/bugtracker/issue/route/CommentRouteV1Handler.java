package com.github.devraghav.bugtracker.issue.route;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.request.CommentRequest;
import com.github.devraghav.bugtracker.issue.response.CommentResponse;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import com.github.devraghav.bugtracker.issue.service.CommentCommandService;
import com.github.devraghav.bugtracker.issue.service.CommentQueryService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommentRouteV1Handler implements CommentRouteHandler {
  private final CommentCommandService commentCommandService;
  private final CommentQueryService commentQueryService;

  @Override
  public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
    var issueId = serverRequest.pathVariable("issueId");
    return commentQueryService
        .getComments(issueId)
        .collectList()
        .flatMap(CommentResponse::retrieve)
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(serverRequest, exception));
  }

  @Override
  public Mono<ServerResponse> save(ServerRequest request) {
    var issueId = request.pathVariable("issueId");
    // @spotless:off
    var principalWithCreateRequestMono =
        Mono.zip(getAuthenticatedPrincipal(request),
            request.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {}));
    return principalWithCreateRequestMono
        .map(tuple2 ->new CommentRequest.CreateComment(
                    tuple2.getT1(), issueId, tuple2.getT2().get("content")))
        .flatMap(commentCommandService::save)
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(CommentResponse.noBody(request))
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(request, exception))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
    // @spotless:on
  }

  @Override
  public Mono<ServerResponse> update(ServerRequest request) {
    var issueId = request.pathVariable("issueId");
    var commentId = request.pathVariable("commentId");
    // @spotless:off
    var principalWithUpdateRequestMono =
        Mono.zip(
            getAuthenticatedPrincipal(request),
            request.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {}));
    return principalWithUpdateRequestMono
        .map(tuple2 ->
                new CommentRequest.UpdateComment(tuple2.getT1(), issueId, commentId, tuple2.getT2().get("content")))
        .flatMap(commentCommandService::update)
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(CommentResponse.noBody(request))
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(request, exception))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
    // @spotless:on
  }

  @Override
  public Mono<ServerResponse> get(ServerRequest request) {
    var commentId = request.pathVariable("commentId");
    return commentQueryService
        .getComment(commentId)
        .flatMap(project -> ServerResponse.ok().body(BodyInserters.fromValue(project)))
        .onErrorResume(
            CommentException.class, exception -> CommentResponse.invalid(request, exception));
  }

  @Override
  public Mono<ServerResponse> subscribeCommentStream(ServerRequest request) {
    var issueId = request.pathVariable("issueId");
    return ServerResponse.ok()
        .body(BodyInserters.fromServerSentEvents(commentQueryService.subscribe(issueId)));
  }
}
