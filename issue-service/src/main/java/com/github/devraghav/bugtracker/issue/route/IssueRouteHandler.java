package com.github.devraghav.bugtracker.issue.route;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import com.github.devraghav.bugtracker.issue.service.CommentCommandService;
import com.github.devraghav.bugtracker.issue.service.IssueCommandService;
import com.github.devraghav.bugtracker.issue.service.IssueQueryService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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
    IssueCommandService issueCommandService,
    IssueQueryService issueQueryService,
    CommentCommandService commentCommandService) {

  public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
    IssueFilter issueFilter = new IssueFilter();
    issueFilter.setProjectId(serverRequest.queryParam("projectId"));
    issueFilter.setReportedBy(serverRequest.queryParam("reportedBy"));
    return issueQueryService
        .findAllByFilter(issueFilter)
        .collectList()
        .flatMap(IssueResponse::retrieve)
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(serverRequest, exception));
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(CreateIssueRequest.class)
        .flatMap(issueCommandService::create)
        .flatMap(issue -> IssueResponse.create(request, issue))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> update(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(UpdateIssueRequest.class)
        .flatMap(updateRequest -> issueCommandService.update(issueId, updateRequest))
        .flatMap(issue -> IssueResponse.create(request, issue))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    var id = request.pathVariable("id");
    return issueQueryService
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
        .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
        .mapNotNull(body -> body.get("user"))
        .map(user -> new AssignRequest(issueId, user, MonitorType.ASSIGN))
        .switchIfEmpty(Mono.just(new AssignRequest(issueId, null, MonitorType.UNASSIGN)))
        .flatMap(assignRequest -> issueCommandService.monitor(issueId, assignRequest))
        .then(IssueResponse.noContent())
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(serverRequest, exception))
        .switchIfEmpty(IssueResponse.noBody(serverRequest));
  }

  public Mono<ServerResponse> watch(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
        .map(body -> new AssignRequest(issueId, body.get("user"), MonitorType.WATCH))
        .flatMap(assignRequest -> issueCommandService.monitor(issueId, assignRequest))
        .then(IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> unwatch(ServerRequest request) {
    var issueId = request.pathVariable("id");

    return request
        .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
        .map(body -> new AssignRequest(issueId, body.get("user"), MonitorType.UNWATCH))
        .flatMap(assignRequest -> issueCommandService.monitor(issueId, assignRequest))
        .then(IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception))
        .switchIfEmpty(IssueResponse.noBody(request));
  }

  public Mono<ServerResponse> addAttachment(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return request
        .body(BodyExtractors.toParts())
        .filter(part -> part instanceof FilePart)
        .ofType(FilePart.class)
        .single()
        .flatMap(filePart -> issueCommandService.uploadAttachment(issueId, filePart))
        .flatMap(
            uploadFileHex ->
                ServerResponse.ok()
                    .body(BodyInserters.fromValue(Map.of("fileUploadId", uploadFileHex))));
  }

  public Mono<ServerResponse> resolve(ServerRequest request) {
    return Mono.just(request.pathVariable("id"))
        .flatMap(issueCommandService::resolve)
        .then(IssueResponse.noContent())
        .onErrorResume(
            IssueException.class,
            issueNotFoundException -> IssueResponse.invalid(request, issueNotFoundException));
  }
}
