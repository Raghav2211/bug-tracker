package com.github.devraghav.bugtracker.project.route;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.service.ProjectService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class ProjectRouteHandler {
  private final ProjectService projectService;

  public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
    // @spotless:off
    return projectService
        .findAll()
        .collectList()
        .flatMap(ProjectResponse::retrieve)
        .onErrorResume(ProjectException.class, exception -> ProjectResponse.invalid(serverRequest, exception));
    //@spotless:on
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    // @spotless:off
    var principalWithCreateRequestMono =
        Mono.zip(getAuthenticatedPrincipal(request), request.bodyToMono(ProjectRequest.Create.class));
    return principalWithCreateRequestMono
        .flatMap(tuple2 -> projectService.save(tuple2.getT1(), tuple2.getT2()))
        .flatMap(project -> ProjectResponse.create(request, project))
        .switchIfEmpty(ProjectResponse.noBody(request))
        .onErrorResume(ProjectException.class, exception -> ProjectResponse.invalid(request, exception));
    // @spotless:on
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    var projectId = request.pathVariable("id");
    // @spotless:off
    return projectService
        .findById(projectId)
        .flatMap(project -> ServerResponse.ok().body(BodyInserters.fromValue(project)))
        .onErrorResume(ProjectException.class, exception -> ProjectResponse.notFound(request, exception));
    // @spotless:on
  }

  public Mono<ServerResponse> addVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    // @spotless:off
    var principalWithCreateRequestMono =
        Mono.zip(getAuthenticatedPrincipal(request),request.bodyToMono(ProjectRequest.CreateVersion.class));

    return principalWithCreateRequestMono
        .flatMap(tuple2 -> projectService.addVersionToProjectId(tuple2.getT1(), projectId, tuple2.getT2()))
        .flatMap(ProjectResponse::ok)
        .onErrorResume(ProjectException.class, exception -> ProjectResponse.invalid(request, exception));
    // @spotless:on
  }

  public Mono<ServerResponse> getAllProjectVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(projectService.findAllVersionByProjectId(projectId), Version.class);
  }

  public Mono<ServerResponse> getProjectVersionById(ServerRequest request) {
    var projectId = request.pathVariable("id");
    var versionId = request.pathVariable("versionId");
    // @spotless:off
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(projectService.findVersionByProjectIdAndVersionId(projectId, versionId), Version.class);
    // @spotless:on
  }

  private Mono<String> getAuthenticatedPrincipal(ServerRequest request) {
    return request
        .principal()
        .cast(UsernamePasswordAuthenticationToken.class)
        .map(UsernamePasswordAuthenticationToken::getPrincipal)
        .map(Objects::toString);
  }
}
