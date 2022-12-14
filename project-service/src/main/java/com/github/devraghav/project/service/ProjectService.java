package com.github.devraghav.project.service;

import com.github.devraghav.project.dto.*;
import com.github.devraghav.project.entity.ProjectEntity;
import com.github.devraghav.project.entity.ProjectVersionEntity;
import com.github.devraghav.project.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProjectService {

  private final String userFindByIdURL;
  private final WebClient webClient;
  private final ProjectRepository projectRepository;

  public ProjectService(
      @Value("${app.external.user-service.url}") String userServiceURL,
      WebClient webClient,
      ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
    this.webClient = webClient;
    this.userFindByIdURL = userServiceURL + "/api/rest/v1/user/{id}";
  }

  public Mono<Project> save(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .flatMap(this::validate)
        .map(ProjectEntity::new)
        .flatMap(projectRepository::save)
        .flatMap(this::getProject)
        .onErrorResume(
            DuplicateKeyException.class,
            exception ->
                Mono.error(ProjectException.alreadyExistsByName(projectRequest.getName())));
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
        .map(ProjectVersionEntity::new)
        .flatMap(
            projectVersionEntity -> projectRepository.saveVersion(projectId, projectVersionEntity))
        .map(ProjectVersion::new);
  }

  public Flux<ProjectVersion> findAllVersionByProjectId(String projectId) {
    return projectRepository.findAllVersionByProjectId(projectId).map(ProjectVersion::new);
  }

  public Mono<ProjectVersion> findVersionByProjectIdAndVersionId(
      String projectId, String versionId) {
    return projectRepository
        .findVersionByProjectIdAndVersionId(projectId, versionId)
        .map(ProjectVersion::new);
  }

  public Mono<Project> getProject(ProjectEntity projectEntity) {
    return getUser(projectEntity.getAuthor()).map(author -> new Project(projectEntity, author));
  }

  public Mono<ProjectRequest> validate(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .and(validateName(projectRequest))
        .and(validateDescription(projectRequest))
        .and(validateAuthor(projectRequest))
        .thenReturn(projectRequest);
  }

  private Mono<ProjectRequest> validateName(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .filter(ProjectRequest::isNameValid)
        .switchIfEmpty(Mono.error(() -> ProjectException.invalidName(projectRequest.getName())));
  }

  private Mono<ProjectRequest> validateDescription(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .filter(ProjectRequest::isDescriptionValid)
        .switchIfEmpty(
            Mono.error(() -> ProjectException.invalidDescription(projectRequest.getDescription())));
  }

  private Mono<ProjectRequest> validateAuthor(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .filter(ProjectRequest::isAuthorNotNull)
        .map(ProjectRequest::getAuthor)
        .flatMap(this::validateAuthor)
        .switchIfEmpty(Mono.error(ProjectException::nullAuthor))
        .thenReturn(projectRequest);
  }

  private Mono<Boolean> validateAuthor(String author) {
    return getUser(author)
        .map(User::hasWriteAccess)
        .map(Boolean::booleanValue)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.authorNotHaveWriteAccess(author)));
  }

  private Mono<User> getUser(String authorId) {
    return webClient
        .get()
        .uri(userFindByIdURL, authorId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(ProjectException.authorNotFound(authorId)))
        .bodyToMono(User.class);
  }
}
