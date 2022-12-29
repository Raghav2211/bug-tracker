package com.github.devraghav.bugtracker.issue.route;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import com.github.devraghav.bugtracker.issue.service.IssueCommentService;
import com.github.devraghav.bugtracker.issue.service.IssueService;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
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
        .bodyToMono(CreateIssueRequest.class)
        .flatMap(issueRequest -> issueService.create(UUID.randomUUID().toString(), issueRequest))
        .flatMap(issue -> IssueResponse.create(request, issue))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> update(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(UpdateIssueRequest.class)
        .flatMap(
            updateRequest ->
                issueService.update(UUID.randomUUID().toString(), issueId, updateRequest))
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

  public Mono<ServerResponse> uploadFile(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .body(BodyExtractors.toParts())
        .filter(part -> part instanceof FilePart)
        .ofType(FilePart.class).single()
        .flatMap(filePart -> issueService.uploadAttachment(issueId , filePart))
        .flatMap(
            uploadFileHex ->
                ServerResponse.ok().body(BodyInserters.fromValue(Map.of("fileUploadId", uploadFileHex))));
  }

  public Mono<ServerResponse> assignee(ServerRequest serverRequest) {
    var issueId = serverRequest.pathVariable("id");
    return serverRequest
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(
            assignRequest ->
                issueService.assignee(UUID.randomUUID().toString(), issueId, assignRequest))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(serverRequest, exception))
        .switchIfEmpty(IssueResponse.noBody(serverRequest));
  }

  public Mono<ServerResponse> watch(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(
            assignRequest ->
                issueService.watch(UUID.randomUUID().toString(), issueId, assignRequest, true))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> unwatch(ServerRequest request) {
    var issueId = request.pathVariable("id");

    return request
        .bodyToMono(IssueAssignRequest.class)
        .flatMap(
            assignRequest ->
                issueService.watch(UUID.randomUUID().toString(), issueId, assignRequest, false))
        .flatMap(unused -> IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> addComment(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(CreateCommentRequest.class)
        .flatMap(
            commentRequest ->
                issueCommentService.save(UUID.randomUUID().toString(), issueId, commentRequest))
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> updateComment(ServerRequest request) {
    var issueId = request.pathVariable("id");
    var commentId = request.pathVariable("commentId");
    return request
        .bodyToMono(UpdateCommentRequest.class)
        .flatMap(
            commentRequest ->
                issueCommentService.update(
                    UUID.randomUUID().toString(), issueId, commentId, commentRequest))
        .flatMap(issueComment -> ServerResponse.ok().body(BodyInserters.fromValue(issueComment)))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> resolve(ServerRequest request) {
    return Mono.just(request.pathVariable("id"))
        .flatMap(issueId -> issueService.resolve(UUID.randomUUID().toString(), issueId))
        .flatMap(done -> IssueResponse.noContent())
        .onErrorResume(
            IssueException.class,
            issueNotFoundException -> IssueResponse.invalid(request, issueNotFoundException));
  }
}
