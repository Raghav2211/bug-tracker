package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.event.internal.ProjectEvent;
import com.github.devraghav.bugtracker.project.exception.ProjectException;
import com.github.devraghav.bugtracker.project.mapper.ProjectMapper;
import com.github.devraghav.bugtracker.project.mapper.ProjectVersionMapper;
import com.github.devraghav.bugtracker.project.repository.ProjectRepository;
import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import com.github.devraghav.bugtracker.project.response.ProjectResponse;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectMapper projectMapper;
  private final ProjectVersionMapper projectVersionMapper;
  private final RequestValidator requestValidator;
  private final ProjectRepository projectRepository;
  private final EventBus.ReactivePublisher<DomainEvent> eventReactivePublisher;

  public Mono<ProjectResponse.Project> save(
      String requestBy, ProjectRequest.CreateProject createProject) {
    // @spotless:off
    return requestValidator
        .validate(createProject)
        .map(validRequest -> projectMapper.requestToEntity(requestBy, validRequest))
        .flatMap(projectEntity ->
                upsert(projectEntity,project -> eventReactivePublisher.publish(new ProjectEvent.Created(project))))
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(ProjectException.alreadyExistsByName(createProject.name())));
    // @spotless:on
  }

  public Mono<ProjectResponse.Project> update(
      String requestBy, String projectId, ProjectRequest.UpdateProject updateProject) {
    // @spotless:off
    return requestValidator
            .validate(updateProject).zipWith(projectRepository.findById(projectId))
            .map(tuple2 -> projectMapper.requestToEntity(requestBy, tuple2.getT2(), tuple2.getT1()))
            .flatMap(projectEntity ->
                    upsert(projectEntity,project -> eventReactivePublisher.publish(new ProjectEvent.Updated(project))));
    // @spotless:on
  }

  public Flux<ProjectResponse.Project> findAll() {
    return projectRepository.findAll().map(projectMapper::entityToResponse);
  }

  public Mono<ProjectResponse.Project> findById(String projectId) {
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

  public Mono<ProjectResponse.Version> addVersionToProjectId(
      String requestBy, String projectId, ProjectRequest.CreateVersion createVersion) {
    return exists(projectId)
        .thenReturn(createVersion)
        .map(validRequest -> projectVersionMapper.requestToEntity(requestBy, validRequest))
        .flatMap(projectVersionEntity -> addVersion(projectId, projectVersionEntity));
  }

  public Flux<ProjectResponse.Version> findAllVersionByProjectId(String projectId) {
    return projectRepository
        .findAllVersionByProjectId(projectId)
        .map(projectVersionMapper::entityToResponse);
  }

  public Mono<ProjectResponse.Version> findVersionByProjectIdAndVersionId(
      String projectId, String versionId) {
    return projectRepository
        .findVersionByProjectIdAndVersionId(projectId, versionId)
        .map(projectVersionMapper::entityToResponse);
  }

  private Mono<ProjectResponse.Project> upsert(
      ProjectEntity projectEntity, Consumer<ProjectResponse.Project> onSuccessConsumer) {
    return projectRepository
        .save(projectEntity)
        .map(author -> projectMapper.entityToResponse(projectEntity))
        .doOnSuccess(onSuccessConsumer);
  }

  private Mono<ProjectResponse.Version> addVersion(
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
