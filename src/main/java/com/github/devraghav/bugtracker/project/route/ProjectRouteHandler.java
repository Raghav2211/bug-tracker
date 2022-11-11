package com.github.devraghav.bugtracker.project.route;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.repository.ProjectAlreadyExistsException;
import com.github.devraghav.bugtracker.project.repository.ProjectNotFoundException;
import com.github.devraghav.bugtracker.project.repository.ProjectRepository;
import com.github.devraghav.bugtracker.project.repository.ProjectVersionRepository;
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
public class ProjectRouteHandler {
  private final ProjectService projectService;
  private final ProjectRepository projectRepository;
  private final ProjectVersionRepository projectVersionRepository;

  public Mono<ServerResponse> getAll(ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(projectRepository.findAll().flatMap(projectService::getProject), Project.class);
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(ProjectRequest.class)
        .flatMap(projectService::validate)
        .map(ProjectEntity::new)
        .flatMap(projectRepository::save)
        .flatMap(projectService::getProject)
        .flatMap(project -> ProjectResponse.create(request, project))
        .switchIfEmpty(ProjectResponse.noBody(request))
        .onErrorResume(
            ProjectException.class, exception -> ProjectResponse.invalid(request, exception))
        .onErrorResume(
            ProjectAlreadyExistsException.class,
            exception -> ProjectResponse.alreadyExists(request, exception));
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return projectService
        .findById(projectId)
        .flatMap(project -> ServerResponse.ok().body(BodyInserters.fromValue(project)))
        .onErrorResume(
            ProjectNotFoundException.class,
            exception -> ProjectResponse.notFound(request, exception));
  }

  public Mono<ServerResponse> addVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return request
        .bodyToMono(ProjectVersionRequest.class)
        .flatMap(
            projectVersionRequest ->
                projectService.exists(projectId).thenReturn(projectVersionRequest))
        .map(ProjectVersionEntity::new)
        .flatMap(
            projectVersionEntity -> projectVersionRepository.save(projectId, projectVersionEntity))
        .map(ProjectVersion::new)
        .flatMap(ProjectResponse::ok)
        .onErrorResume(
            ProjectException.class, exception -> ProjectResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> getAllProjectVersion(ServerRequest request) {
    var projectId = request.pathVariable("id");
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            projectVersionRepository.findAll(projectId).map(ProjectVersion::new),
            ProjectVersion.class);
  }
}
