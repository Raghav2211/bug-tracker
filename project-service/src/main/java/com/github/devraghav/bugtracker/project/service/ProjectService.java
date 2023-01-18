package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.event.internal.ProjectEvent;
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
    UserReactiveClient userReactiveClient,
    ProjectRepository projectRepository,
    EventBus.ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<Project> save(String author, ProjectRequest.Create createProjectRequest) {
    // @spotless:off
    return requestValidator
        .validate(author, createProjectRequest)
        .map(projectMapper::requestToEntity)
        .flatMap(this::save)
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(ProjectException.alreadyExistsByName(createProjectRequest.name())));
    // @spotless:on
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
      String userId, String projectId, ProjectRequest.CreateVersion createVersionRequest) {
    return Mono.zip(requestValidator.validateAuthor(userId), exists(projectId))
        .thenReturn(createVersionRequest)
        .map(validRequest -> projectVersionMapper.requestToEntity(userId, validRequest))
        .flatMap(projectVersionEntity -> addVersion(userId, projectId, projectVersionEntity));
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
        .doOnSuccess(project -> eventReactivePublisher.publish(new ProjectEvent.Created(project)));
  }

  private Mono<Version> addVersion(
      String userId, String projectId, ProjectVersionEntity projectVersionEntity) {
    // @spotless:off
    return projectRepository
        .saveVersion(projectId, projectVersionEntity)
        .map(projectVersionMapper::entityToResponse)
        .doOnSuccess(version ->
                eventReactivePublisher.publish(new ProjectEvent.VersionCreated(userId, projectId, version)));
    // @spotless:on
  }

  private Mono<User> fetchAuthor(String authorId) {
    // @spotless:off
    return userReactiveClient
        .fetchUser(authorId)
        .onErrorResume(UserClientException.class,
                exception -> Mono.error(ProjectException.userServiceException(exception)));
    // @spotless:on
  }
}
