package com.github.devraghav.bugtracker.project.route;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.exception.ProjectException;
import com.github.devraghav.bugtracker.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class ProjectRouteV1Handler implements RouteHandler {
  private final ProjectService projectService;

  public Mono<ServerResponse> getAllProjects(ServerRequest serverRequest) {
    // @spotless:off
    return projectService
        .findAll()
        .collectList()
        .flatMap(RequestResponse::retrieve)
        .onErrorResume(ProjectException.class,
                exception -> RequestResponse.invalid(serverRequest, exception));
    //@spotless:on
  }

  @Override
  public Mono<ServerResponse> createProject(ServerRequest request) {
    // @spotless:off
    var principalWithCreateRequestMono =
        Mono.zip(getAuthenticatedPrincipal(request), request.bodyToMono(RequestResponse.CreateProjectRequest.class));
    return principalWithCreateRequestMono
        .flatMap(tuple2 -> projectService.save(tuple2.getT1(), tuple2.getT2()))
        .flatMap(project -> RequestResponse.create(request, project))
        .switchIfEmpty(RequestResponse.noBody(request))
        .onErrorResume(ProjectException.class, exception -> RequestResponse.invalid(request, exception));
    // @spotless:on
  }

  @Override
  public Mono<ServerResponse> getProject(ServerRequest request) {
    var projectId = request.pathVariable("id");
    // @spotless:off
    return projectService
        .findById(projectId)
        .flatMap(project -> ServerResponse.ok().body(BodyInserters.fromValue(project)))
        .onErrorResume(ProjectException.class,
                exception -> RequestResponse.notFound(request, exception));
    // @spotless:on
  }

  @Override
  public Mono<ServerResponse> addVersionToProject(ServerRequest request) {
    var projectId = request.pathVariable("id");
    // @spotless:off
    var principalWithCreateRequestMono =
        Mono.zip(getAuthenticatedPrincipal(request),request.bodyToMono(RequestResponse.CreateVersionRequest.class));

    return principalWithCreateRequestMono
        .flatMap(tuple2 -> projectService.addVersionToProjectId(tuple2.getT1(), projectId, tuple2.getT2()))
        .flatMap(RequestResponse::ok)
        .onErrorResume(ProjectException.class, exception -> RequestResponse.invalid(request, exception));
    // @spotless:on
  }

  @Override
  public Mono<ServerResponse> getAllProjectVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            projectService.findAllVersionByProjectId(projectId),
            RequestResponse.VersionResponse.class);
  }

  @Override
  public Mono<ServerResponse> getProjectVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    var versionId = request.pathVariable("versionId");
    // @spotless:off
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(projectService.findVersionByProjectIdAndVersionId(projectId, versionId),
                RequestResponse.VersionResponse.class);
    // @spotless:on
  }
}
