package com.github.devraghav.bugtracker.issue.route.handler;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import com.github.devraghav.bugtracker.issue.service.IssueCommandService;
import com.github.devraghav.bugtracker.issue.service.IssueQueryService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
class IssueRouteV1Handler implements IssueRouteHandler {

  private final IssueCommandService issueCommandService;
  private final IssueQueryService issueQueryService;

  @Override
  public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
    IssueFilter issueFilter =
        IssueFilter.builder()
            .projectId(serverRequest.queryParam("projectId").orElseGet(() -> null))
            .reportedBy(serverRequest.queryParam("reportedBy").orElseGet(() -> null))
            .pageRequest(IssueRequest.Page.of(serverRequest))
            .build();
    return issueQueryService
        .findAllByFilter(issueFilter)
        .collectList()
        .log()
        .zipWith(issueQueryService.count())
        .map(tuple -> new PageImpl<>(tuple.getT1(), issueFilter.getPageRequest(), tuple.getT2()))
        .flatMap(IssueResponse::retrieve)
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(serverRequest, exception));
  }

  @Override
  public Mono<ServerResponse> create(ServerRequest request) {
    return Mono.zip(
            getAuthenticatedPrincipal(request), request.bodyToMono(IssueRequest.CreateIssue.class))
        .flatMap(tuple2 -> issueCommandService.create(tuple2.getT1(), tuple2.getT2()))
        .flatMap(issue -> IssueResponse.create(request, issue))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  @Override
  public Mono<ServerResponse> update(ServerRequest request) {
    var issueId = request.pathVariable("id");
    return Mono.zip(
            getAuthenticatedPrincipal(request), request.bodyToMono(IssueRequest.UpdateIssue.class))
        .flatMap(tuple2 -> issueCommandService.update(tuple2.getT1(), issueId, tuple2.getT2()))
        .flatMap(issue -> IssueResponse.create(request, issue))
        .switchIfEmpty(IssueResponse.noBody(request))
        .onErrorResume(
            IssueException.class, exception -> IssueResponse.invalid(request, exception));
  }

  @Override
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

  @Override
  public Mono<ServerResponse> monitor(ServerRequest request, IssueRequest.MonitorType monitorType) {
    var issueId = request.pathVariable("id");
    // @spotless:off
    return Mono.zip(
            getAuthenticatedPrincipal(request),
            request.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {}))
        .map(tuple2 -> new IssueRequest.Monitor(issueId, tuple2.getT2().get("user"), monitorType, tuple2.getT1()))
        .flatMap(monitor -> issueCommandService.monitor(issueId, monitor))
        .then(IssueResponse.noContent())
        .onErrorResume(IssueException.class, exception -> IssueResponse.invalid(request, exception));
    // @spotless:on
  }

  @Override
  public Mono<ServerResponse> addAttachment(ServerRequest request) {
    var issueId = request.pathVariable("id");
    // @spotless:off
    return request
        .body(BodyExtractors.toParts())
        .filter(part -> part instanceof FilePart)
        .ofType(FilePart.class)
        .single()
        .flatMap(filePart -> issueCommandService.uploadAttachment(issueId, filePart))
        .flatMap(uploadFileHex ->ServerResponse.ok().body(BodyInserters.fromValue(Map.of("fileUploadId", uploadFileHex))));
    // @spotless:on
  }

  @Override
  public Mono<ServerResponse> resolve(ServerRequest request) {
    return getAuthenticatedPrincipal(request)
        .flatMap(principal -> issueCommandService.resolve(request.pathVariable("id"), principal))
        .then(IssueResponse.noContent())
        .onErrorResume(
            IssueException.class,
            issueNotFoundException -> IssueResponse.invalid(request, issueNotFoundException));
  }
}
