package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
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
    ProjectRepository projectRepository) {

  public Mono<Project> save(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .flatMap(this::validate)
        .map(projectMapper::requestToEntity)
        .flatMap(projectRepository::save)
        .flatMap(this::getProject)
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(ProjectException.alreadyExistsByName(projectRequest.name())));
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
        .flatMap(
            _projectVersionRequest -> this.exists(projectId).thenReturn(_projectVersionRequest))
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
