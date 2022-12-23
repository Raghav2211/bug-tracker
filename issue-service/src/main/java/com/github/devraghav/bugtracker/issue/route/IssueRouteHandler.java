package com.github.devraghav.bugtracker.issue.route;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import com.github.devraghav.bugtracker.issue.service.IssueCommentService;
import com.github.devraghav.bugtracker.issue.service.IssueService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public record IssueRouteHandler(
    IssueService issueService, IssueCommentService issueCommentService) {

  public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
    IssueFilter issueFilter = new IssueFilter();
    issueFilter.setProjectId(serverRequest.queryParam("projectId"));
    issueFilter.setReportedBy(serverRequest.queryParam("reportedBy"));
    return issueService
        .getAll(issueFilter)
        .collectList()
        .flatMap(IssueResponse::retrieve)
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(serverRequest, exception));
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(IssueRequest.class)
        .flatMap(issueService::create)
        .flatMap(issue -> IssueResponse.create(request, issue))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> update(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(IssueUpdateRequest.class)
        .flatMap(updateRequest -> issueService.update(issueId, updateRequest))
        .flatMap(issue -> IssueResponse.create(request, issue))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    var id = request.pathVariable("id");
    return issueService
        .get(id)
        .flatMap(issue -> ServerResponse.ok().body(BodyInserters.fromValue(issue)))
        .onErrorResume(
            IssueNotFoundException.class, exception -> IssueResponse.notFound(request, exception))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> assignee(ServerRequest serverRequest) {
    var issueId = serverRequest.pathVariable("id");
    return serverRequest
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(assignRequest -> issueService.assignee(issueId, assignRequest))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(serverRequest, exception))
        .switchIfEmpty(IssueResponse.noBody(serverRequest));
  }

  public Mono<ServerResponse> watch(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(assignRequest -> issueService.watch(issueId, assignRequest, true))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> unWatch(ServerRequest request) {
    var issueId = request.pathVariable("id");

    return request
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(assignRequest -> issueService.watch(issueId, assignRequest, false))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> addComment(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(IssueCommentRequest.class)
        .flatMap(commentRequest -> issueCommentService.save(issueId, commentRequest))
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> done(ServerRequest request) {
    return Mono.just(request.pathVariable("id"))
        .flatMap(issueService::done)
        .flatMap(done -> IssueResponse.noContent())
        .onErrorResume(
            IssueException.class,
            issueNotFoundException -> IssueResponse.invalid(request, issueNotFoundException));
  }
}
