package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.event.internal.ProjectEvent;
import com.github.devraghav.bugtracker.project.exception.ProjectException;
import com.github.devraghav.bugtracker.project.mapper.ProjectMapper;
import com.github.devraghav.bugtracker.project.mapper.ProjectVersionMapper;
import com.github.devraghav.bugtracker.project.repository.ProjectRepository;
import com.github.devraghav.bugtracker.project.validation.RequestValidator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record ProjectService(
    ProjectMapper projectMapper,
    ProjectVersionMapper projectVersionMapper,
    RequestValidator requestValidator,
    ProjectRepository projectRepository,
    EventBus.ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<RequestResponse.ProjectResponse> save(
      String requestBy, RequestResponse.CreateProjectRequest createProjectRequest) {
    // @spotless:off
    return requestValidator
        .validate( createProjectRequest)
        .map(validRequest -> projectMapper.requestToEntity(requestBy, validRequest))
        .flatMap(this::save)
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(ProjectException.alreadyExistsByName(createProjectRequest.name())));
    // @spotless:on
  }

  public Flux<RequestResponse.ProjectResponse> findAll() {
    return projectRepository.findAll().map(projectMapper::entityToResponse);
  }

  public Mono<RequestResponse.ProjectResponse> findById(String projectId) {
    return projectRepository
        .findById(projectId)
        .map(projectMapper::entityToResponse)
        .switchIfEmpty(Mono.error(() -> ProjectException.notFound(projectId)));
  }

  public Mono<Boolean> exists(String projectId) {
    return projectRepository
        .existsById(projectId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.notFound(projectId)));
  }

  public Mono<RequestResponse.VersionResponse> addVersionToProjectId(
      String requestBy,
      String projectId,
      RequestResponse.CreateVersionRequest createVersionRequest) {
    return exists(projectId)
        .thenReturn(createVersionRequest)
        .map(validRequest -> projectVersionMapper.requestToEntity(requestBy, validRequest))
        .flatMap(projectVersionEntity -> addVersion(projectId, projectVersionEntity));
  }

  public Flux<RequestResponse.VersionResponse> findAllVersionByProjectId(String projectId) {
    return projectRepository
        .findAllVersionByProjectId(projectId)
        .map(projectVersionMapper::entityToResponse);
  }

  public Mono<RequestResponse.VersionResponse> findVersionByProjectIdAndVersionId(
      String projectId, String versionId) {
    return projectRepository
        .findVersionByProjectIdAndVersionId(projectId, versionId)
        .map(projectVersionMapper::entityToResponse);
  }

  private Mono<RequestResponse.ProjectResponse> save(ProjectEntity projectEntity) {
    return projectRepository
        .save(projectEntity)
        .map(author -> projectMapper.entityToResponse(projectEntity))
        .doOnSuccess(project -> eventReactivePublisher.publish(new ProjectEvent.Created(project)));
  }

  private Mono<RequestResponse.VersionResponse> addVersion(
      String projectId, ProjectVersionEntity projectVersionEntity) {
    // @spotless:off
    return projectRepository
        .saveVersion(projectId, projectVersionEntity)
        .map(projectVersionMapper::entityToResponse)
        .doOnSuccess(version ->
                eventReactivePublisher.publish(new ProjectEvent.VersionCreated(projectId, version)));
    // @spotless:on
  }
}
