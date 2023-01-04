package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.kafka.producer.KafkaProducer;
import com.github.devraghav.bugtracker.project.mapper.ProjectMapper;
import com.github.devraghav.bugtracker.project.mapper.ProjectVersionMapper;
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

  public Mono<Project> save(String requestId, CreateProjectRequest createProjectRequest) {
    return Mono.just(createProjectRequest)
        .flatMap(this::validate)
        .flatMap(request -> kafkaProducer.sendProjectCreateCommand(requestId, request))
        .map(projectMapper::requestToEntity)
        .flatMap(entity -> save(requestId, entity))
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> duplicateProject(requestId, createProjectRequest, exception));
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
      String requestId, String projectId, CreateProjectVersionRequest createProjectVersionRequest) {
    return Mono.just(createProjectVersionRequest)
        .flatMap(versionRequest -> this.exists(projectId).thenReturn(versionRequest))
        .flatMap(
            validVersionRequest ->
                kafkaProducer.sendProjectVersionCreateCommand(
                    requestId, projectId, validVersionRequest))
        .map(projectVersionMapper::requestToEntity)
        .flatMap(projectVersionEntity -> save(requestId, projectId, projectVersionEntity));
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

  public Mono<CreateProjectRequest> validate(CreateProjectRequest createProjectRequest) {
    return createProjectRequest
        .validate()
        .and(fetchAndValidateAuthorAccess(createProjectRequest.author()))
        .thenReturn(createProjectRequest);
  }

  private Mono<Project> save(String requestId, ProjectEntity projectEntity) {
    return projectRepository
        .save(projectEntity)
        .flatMap(this::getProject)
        .flatMap(project -> kafkaProducer.sendProjectCreatedEvent(requestId, project));
  }

  private Mono<ProjectVersion> save(
      String requestId, String projectId, ProjectVersionEntity projectVersionEntity) {
    return projectRepository
        .saveVersion(projectId, projectVersionEntity)
        .map(projectVersionMapper::entityToResponse)
        .flatMap(
            projectVersion ->
                kafkaProducer.sendProjectVersionCreatedEvent(requestId, projectId, projectVersion));
  }

  private Mono<Project> duplicateProject(
      String requestId,
      CreateProjectRequest createProjectRequest,
      DuplicateKeyException exception) {
    return kafkaProducer
        .sendProjectDuplicatedEvent(requestId, createProjectRequest)
        .flatMap(
            unused ->
                Mono.error(ProjectException.alreadyExistsByName(createProjectRequest.name())));
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
