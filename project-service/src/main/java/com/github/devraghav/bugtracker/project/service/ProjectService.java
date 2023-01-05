package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.kafka.producer.KafkaProducer;
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
    KafkaProducer kafkaProducer) {

  public Mono<Project> save(CreateProjectRequest createProjectRequest) {
    return Mono.just(createProjectRequest)
        .flatMap(requestValidator::validate)
        .map(projectMapper::requestToEntity)
        .flatMap(this::save)
        .onErrorResume(
            DuplicateKeyException.class, exception -> duplicateProject(createProjectRequest));
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
      String projectId, CreateProjectVersionRequest createProjectVersionRequest) {
    return exists(projectId)
        .thenReturn(createProjectVersionRequest)
        .map(projectVersionMapper::requestToEntity)
        .flatMap(projectVersionEntity -> save(projectId, projectVersionEntity));
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

  private Mono<Project> save(ProjectEntity projectEntity) {
    return projectRepository
        .save(projectEntity)
        .flatMap(this::getProject)
        .flatMap(kafkaProducer::sendProjectCreatedEvent);
  }

  private Mono<ProjectVersion> save(String projectId, ProjectVersionEntity projectVersionEntity) {
    return projectRepository
        .saveVersion(projectId, projectVersionEntity)
        .map(projectVersionMapper::entityToResponse)
        .flatMap(
            projectVersion ->
                kafkaProducer.sendProjectVersionCreatedEvent(projectId, projectVersion));
  }

  private Mono<Project> duplicateProject(CreateProjectRequest createProjectRequest) {
    return kafkaProducer
        .sendProjectDuplicatedEvent(createProjectRequest)
        .flatMap(
            unused ->
                Mono.error(ProjectException.alreadyExistsByName(createProjectRequest.name())));
  }

  private Mono<User> fetchAuthor(String authorId) {
    return userReactiveClient
        .fetchUser(authorId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(ProjectException.userServiceException(exception)));
  }
}
