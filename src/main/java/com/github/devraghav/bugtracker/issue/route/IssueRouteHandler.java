package com.github.devraghav.bugtracker.issue.route;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.service.IssueCommentPersistService;
import com.github.devraghav.bugtracker.issue.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class IssueRouteHandler {
  private final IssueService issueService;
  private final IssueCommentPersistService issueCommentPersistService;

  private final IssueRepository issueRepository;

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

  public Mono<ServerResponse> assign(ServerRequest serverRequest) {
    var issueId = serverRequest.pathVariable("id");
    return serverRequest
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(
            assignRequest ->
                Mono.just(issueId)
                    .and(issueService.exists(issueId))
                    .and(issueService.validateUserId(assignRequest.getUser()))
                    .thenReturn(assignRequest))
        .flatMap(assignRequest -> issueRepository.assign(issueId, assignRequest.getUser()))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(serverRequest, exception))
        .switchIfEmpty(IssueResponse.noBody(serverRequest));
  }

  public Mono<ServerResponse> unassign(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(
            assignRequest ->
                Mono.just(issueId)
                    .and(issueService.exists(issueId))
                    .and(issueService.validateUserId(assignRequest.getUser()))
                    .thenReturn(assignRequest))
        .flatMap(assignRequest -> issueRepository.unassign(issueId, assignRequest.getUser()))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> addWatcher(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(
            assignRequest ->
                Mono.just(issueId)
                    .and(issueService.exists(issueId))
                    .and(issueService.validateUserId(assignRequest.getUser()))
                    .thenReturn(assignRequest))
        .flatMap(assignRequest -> issueRepository.addWatcher(issueId, assignRequest.getUser()))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> removeWatcher(ServerRequest request) {
    var issueId = request.pathVariable("id");

    return request
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(
            removeWatcherRequest ->
                Mono.just(issueId)
                    .and(issueService.exists(issueId))
                    .and(issueService.validateUserId(removeWatcherRequest.getUser()))
                    .thenReturn(removeWatcherRequest))
        .flatMap(
            removeWatcherRequest ->
                issueRepository.removeWatcher(issueId, removeWatcherRequest.getUser()))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> addComment(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(IssueCommentRequest.class)
        .flatMap(commentRequest -> issueCommentPersistService.save(issueId, commentRequest))
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> done(ServerRequest request) {
    return Mono.just(request.pathVariable("id"))
        .flatMap(issueRepository::done)
        .flatMap(done -> IssueResponse.noContent())
        .onErrorResume(
            IssueException.class,
            issueNotFoundException -> IssueResponse.invalid(request, issueNotFoundException));
  }
}
