package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.ProjectException;
import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import com.github.devraghav.bugtracker.project.dto.ProjectVersion;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.repository.ProjectRepository;
import com.github.devraghav.bugtracker.project.repository.ProjectVersionRepository;
import com.github.devraghav.bugtracker.user.service.UserNotFoundException;
import com.github.devraghav.bugtracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
  private final ProjectRepository projectRepository;
  private final ProjectVersionRepository projectVersionRepository;
  private final UserService userService;

  public Mono<Project> findById(String projectId) {
    return projectRepository.findById(projectId).flatMap(this::getProject);
  }

  public Mono<Boolean> exists(String projectId) {
    return projectRepository
        .exists(projectId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.projectNotFound(projectId)));
  }

  public Mono<Boolean> exists(String projectId, String versionId) {
    return projectVersionRepository
        .exists(projectId, versionId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.versionNotFound(projectId, versionId)));
  }

  public Mono<Project> getProject(ProjectEntity projectEntity) {
    return Mono.zip(
        userService.findById(projectEntity.getAuthor()),
        projectVersionRepository
            .findAll(projectEntity.getId())
            .map(ProjectVersion::new)
            .collect(Collectors.toSet()),
        (author, versions) -> new Project(projectEntity, author, versions));
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
    return userService
        .hasUserWriteAccess(author)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.authorNotHaveWriteAccess(author)))
        .onErrorResume(
            UserNotFoundException.class,
            exception -> Mono.error(() -> ProjectException.authorNotFound(author)));
  }
}
