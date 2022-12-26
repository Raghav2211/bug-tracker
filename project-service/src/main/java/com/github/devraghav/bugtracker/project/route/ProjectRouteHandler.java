package com.github.devraghav.bugtracker.project.route;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.service.ProjectService;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public record ProjectRouteHandler(ProjectService projectService) {

  private static final Supplier<UUID> REQUEST_ID = UUID::randomUUID;

  public Mono<ServerResponse> getAll(ServerRequest serverRequest) {
    return projectService
        .findAll()
        .collectList()
        .flatMap(ProjectResponse::retrieve)
        .onErrorResume(
            ProjectException.class, exception -> ProjectResponse.invalid(serverRequest, exception));
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(ProjectRequest.class)
        .flatMap(projectRequest -> projectService.save(REQUEST_ID.get().toString(), projectRequest))
        .flatMap(project -> ProjectResponse.create(request, project))
        .switchIfEmpty(ProjectResponse.noBody(request))
        .onErrorResume(
            ProjectException.class, exception -> ProjectResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return projectService
        .findById(projectId)
        .flatMap(project -> ServerResponse.ok().body(BodyInserters.fromValue(project)))
        .onErrorResume(
            ProjectException.class, exception -> ProjectResponse.notFound(request, exception));
  }

  public Mono<ServerResponse> addVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return request
        .bodyToMono(ProjectVersionRequest.class)
        .flatMap(
            projectVersionRequest ->
                projectService.addVersionToProjectId(
                    REQUEST_ID.get().toString(), projectId, projectVersionRequest))
        .flatMap(ProjectResponse::ok)
        .onErrorResume(
            ProjectException.class, exception -> ProjectResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> getAllProjectVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(projectService.findAllVersionByProjectId(projectId), ProjectVersion.class);
  }

  public Mono<ServerResponse> getProjectVersionById(ServerRequest request) {
    var projectId = request.pathVariable("id");
    var versionId = request.pathVariable("versionId");
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            projectService.findVersionByProjectIdAndVersionId(projectId, versionId),
            ProjectVersion.class);
  }
}
