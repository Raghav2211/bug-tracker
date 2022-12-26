package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.mapper.ProjectMapper;
import com.github.devraghav.bugtracker.project.mapper.ProjectVersionMapper;
import com.github.devraghav.bugtracker.project.producer.KafkaProducer;
import com.github.devraghav.bugtracker.project.repository.ProjectRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record ProjectService(
    ProjectMapper projectMapper,
    ProjectVersionMapper projectVersionMapper,
    UserReactiveClient userReactiveClient,
    ProjectRepository projectRepository,
    KafkaProducer kafkaProducer) {

  public Mono<Project> save(String requestId, ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .flatMap(this::validate)
        .flatMap(request -> kafkaProducer.generateAndSendProjectCreateCommand(requestId, request))
        .map(projectMapper::requestToEntity)
        .flatMap(entity -> save(requestId, entity))
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> duplicateProject(requestId, projectRequest, exception));
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

  public Mono<ProjectVersion> addVersionToProjectId(
      String projectId, ProjectVersionRequest projectVersionRequest) {
    return Mono.just(projectVersionRequest)
        .flatMap(versionRequest -> this.exists(projectId).thenReturn(versionRequest))
        .map(projectVersionMapper::requestToEntity)
        .flatMap(
            projectVersionEntity -> projectRepository.saveVersion(projectId, projectVersionEntity))
        .map(projectVersionMapper::entityToResponse);
  }

  public Flux<ProjectVersion> findAllVersionByProjectId(String projectId) {
    return projectRepository
        .findAllVersionByProjectId(projectId)
        .map(projectVersionMapper::entityToResponse);
  }

  public Mono<ProjectVersion> findVersionByProjectIdAndVersionId(
      String projectId, String versionId) {
    return projectRepository
        .findVersionByProjectIdAndVersionId(projectId, versionId)
        .map(projectVersionMapper::entityToResponse);
  }

  public Mono<Project> getProject(ProjectEntity projectEntity) {
    return fetchAuthor(projectEntity.getAuthor())
        .map(author -> projectMapper.entityToResponse(projectEntity).author(author).build());
  }

  public Mono<ProjectRequest> validate(ProjectRequest projectRequest) {
    return projectRequest
        .validate()
        .and(fetchAndValidateAuthorAccess(projectRequest.author()))
        .thenReturn(projectRequest);
  }

  private Mono<Project> save(String requestId, ProjectEntity projectEntity) {
    return projectRepository
        .save(projectEntity)
        .flatMap(this::getProject)
        .flatMap(project -> kafkaProducer.generateAndSendProjectCreatedEvent(requestId, project));
  }

  private Mono<Project> duplicateProject(
      String requestId, ProjectRequest projectRequest, DuplicateKeyException exception) {
    return kafkaProducer
        .generateAndSendProjectDuplicatedEvent(requestId, projectRequest)
        .flatMap(unused -> Mono.error(ProjectException.alreadyExistsByName(projectRequest.name())));
  }

  private Mono<Boolean> fetchAndValidateAuthorAccess(String author) {
    return fetchAuthor(author)
        .map(User::hasWriteAccess)
        .map(Boolean::booleanValue)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.authorNotHaveWriteAccess(author)));
  }

  private Mono<User> fetchAuthor(String authorId) {
    return userReactiveClient
        .fetchUser(authorId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(ProjectException.userServiceException(exception)));
  }
}
