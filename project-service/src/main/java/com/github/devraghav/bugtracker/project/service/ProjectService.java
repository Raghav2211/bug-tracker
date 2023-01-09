package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.project.event.internal.ProjectCreatedEvent;
import com.github.devraghav.bugtracker.project.event.internal.VersionCreatedEvent;
import com.github.devraghav.bugtracker.project.mapper.ProjectMapper;
import com.github.devraghav.bugtracker.project.mapper.ProjectVersionMapper;
import com.github.devraghav.bugtracker.project.pubsub.ReactivePublisher;
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
    UserReactiveClient userReactiveClient,
    ProjectRepository projectRepository,
    ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<Project> save(CreateProjectRequest createProjectRequest) {
    return Mono.just(createProjectRequest)
        .flatMap(requestValidator::validate)
        .map(projectMapper::requestToEntity)
        .flatMap(this::save)
        .onErrorResume(
            DuplicateKeyException.class,
            exception ->
                Mono.error(ProjectException.alreadyExistsByName(createProjectRequest.name())));
  }

  public Flux<Project> findAll() {
    return projectRepository.findAll().flatMap(this::getProject);
  }

  public Mono<Project> findById(String projectId) {
    return projectRepository
        .findById(projectId)
        .flatMap(this::getProject)
        .switchIfEmpty(Mono.error(() -> ProjectException.notFound(projectId)));
  }

  public Mono<Boolean> exists(String projectId) {
    return projectRepository
        .existsById(projectId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.notFound(projectId)));
  }

  public Mono<Version> addVersionToProjectId(
      String projectId, CreateVersionRequest createVersionRequest) {
    return exists(projectId)
        .thenReturn(createVersionRequest)
        .map(projectVersionMapper::requestToEntity)
        .flatMap(projectVersionEntity -> save(projectId, projectVersionEntity));
  }

  public Flux<Version> findAllVersionByProjectId(String projectId) {
    return projectRepository
        .findAllVersionByProjectId(projectId)
        .map(projectVersionMapper::entityToResponse);
  }

  public Mono<Version> findVersionByProjectIdAndVersionId(String projectId, String versionId) {
    return projectRepository
        .findVersionByProjectIdAndVersionId(projectId, versionId)
        .map(projectVersionMapper::entityToResponse);
  }

  public Mono<Project> getProject(ProjectEntity projectEntity) {
    return fetchAuthor(projectEntity.getAuthor())
        .map(author -> projectMapper.entityToResponse(projectEntity).author(author).build());
  }

  private Mono<Project> save(ProjectEntity projectEntity) {
    return projectRepository
        .save(projectEntity)
        .flatMap(this::getProject)
        .flatMap(this::publishProjectCreatedEvent);
  }

  private Mono<Version> save(String projectId, ProjectVersionEntity projectVersionEntity) {
    return projectRepository
        .saveVersion(projectId, projectVersionEntity)
        .map(projectVersionMapper::entityToResponse)
        .flatMap(version -> publishVersionCreatedEvent(projectId, version));
  }

  private Mono<Project> publishProjectCreatedEvent(Project project) {
    return eventReactivePublisher.publish(new ProjectCreatedEvent(project)).thenReturn(project);
  }

  private Mono<Version> publishVersionCreatedEvent(String projectId, Version version) {
    return eventReactivePublisher
        .publish(new VersionCreatedEvent(projectId, version))
        .thenReturn(version);
  }

  private Mono<User> fetchAuthor(String authorId) {
    return userReactiveClient
        .fetchUser(authorId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(ProjectException.userServiceException(exception)));
  }
}
