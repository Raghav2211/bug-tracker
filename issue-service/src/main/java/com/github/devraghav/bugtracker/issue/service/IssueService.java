package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.entity.ProjectInfoRef;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record IssueService(
    IssueMapper issueMapper,
    UserReactiveClient userReactiveClient,
    ProjectReactiveClient projectReactiveClient,
    IssueRepository issueRepository,
    IssueCommentFetchService issueCommentFetchService) {

  public Flux<Issue> getAll(IssueFilter issueFilter) {
    if (issueFilter.getProjectId().isPresent()) {
      return Mono.just(issueFilter.getProjectId())
          .map(Optional::get)
          .flatMapMany(this::getAllByProjectId);

    } else if (issueFilter.getReportedBy().isPresent()) {
      return Mono.just(issueFilter.getReportedBy())
          .map(Optional::get)
          .flatMapMany(this::getAllByReporter);
    }
    return issueRepository.findAll().flatMap(this::generateIssue);
  }

  private Mono<IssueEntity> findById(String issueId) {
    return issueRepository.findById(issueId);
  }

  public Mono<Issue> get(String issueId) {
    return findById(issueId).flatMap(this::generateIssue);
  }

  public Mono<Issue> create(IssueRequest issueRequest) {
    return validate(issueRequest).map(issueMapper::issueRequestToIssueEntity).flatMap(this::save);
  }

  public Mono<Issue> update(String issueId, IssueUpdateRequest request) {
    return findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .map(issueEntity -> issueMapper.issueRequestToIssueEntity(issueEntity, request))
        .flatMap(this::save)
        .switchIfEmpty(Mono.error(() -> IssueException.alreadyEnded(issueId)));
  }

  private Mono<Issue> save(IssueEntity issueEntity) {
    return issueRepository.save(issueEntity).flatMap(this::generateIssue);
  }

  public Mono<Boolean> exists(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }

  public Mono<Long> assign(String issueId, IssueAssignRequest issueAssignRequest) {
    return exists(issueId)
        .and(fetchUser(issueAssignRequest.user()))
        .thenReturn(issueAssignRequest)
        .flatMap(
            assignRequest -> issueRepository.findAndSetAssigneeById(issueId, assignRequest.user()));
  }

  public Mono<Long> unassigned(String issueId) {
    return exists(issueId)
        .flatMap(assignRequest -> issueRepository.findAndUnSetAssigneeById(issueId));
  }

  public Mono<Long> addWatcher(String issueId, IssueAssignRequest issueAssignRequest) {
    return exists(issueId)
        .and(fetchUser(issueAssignRequest.user()))
        .thenReturn(issueAssignRequest)
        .flatMap(
            assignRequest -> issueRepository.findAndAddWatcherById(issueId, assignRequest.user()));
  }

  public Mono<Long> removeWatcher(String issueId, IssueAssignRequest issueAssignRequest) {
    return exists(issueId)
        .and(fetchUser(issueAssignRequest.user()))
        .thenReturn(issueAssignRequest)
        .flatMap(
            assignRequest -> issueRepository.findAndPullWatcherById(issueId, assignRequest.user()));
  }

  public Mono<Boolean> done(String issueId) {
    return issueRepository.done(issueId);
  }

  private Flux<Issue> getAllByProjectId(String projectId) {
    return validateProjectId(projectId)
        .flatMapMany(
            unused -> issueRepository.findAllByProjectId(projectId).flatMap(this::generateIssue));
  }

  private Flux<Issue> getAllByReporter(String reporter) {
    return fetchUser(reporter)
        .flatMapMany(
            unused -> issueRepository.findAllByReporter(reporter).flatMap(this::generateIssue));
  }

  private Mono<Issue> generateIssue(IssueEntity issueEntity) {
    var issueBuilder = issueMapper.issueEntityToIssue(issueEntity);
    return getWatchers(issueEntity.getWatchers())
        .collect(Collectors.toSet())
        .map(issueBuilder::watchers)
        .flatMapMany(unused -> issueCommentFetchService.getComments(issueEntity.getId()))
        .collectList()
        .map(issueBuilder::comments)
        .flatMap(unused -> fetchUser(issueEntity.getReporter()))
        .map(issueBuilder::reporter)
        .flatMapMany(unused -> getProjects(issueEntity.getProjects()))
        .collectList()
        .map(issueBuilder::projects)
        .flatMap(unused -> getAssignee(issueEntity.getAssignee()))
        .map(issueBuilder::assignee)
        .map(unused -> issueBuilder.endedAt(issueEntity.getEndedAt()))
        .map(unused -> issueBuilder.build());
  }

  private Flux<User> getWatchers(Set<String> watchers) {
    return Flux.fromIterable(watchers).flatMap(this::fetchUser);
  }

  private Mono<User> getAssignee(Optional<String> assignee) {
    return Mono.just(assignee)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(this::fetchUser);
  }

  private Flux<Project> getProjects(Set<ProjectInfoRef> projectInfoRefs) {
    return Flux.fromIterable(projectInfoRefs).flatMap(this::getProject);
  }

  private Mono<Project> getProject(ProjectInfoRef projectInfoRef) {
    return projectReactiveClient
        .fetchProject(projectInfoRef.getProjectId())
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)))
        .doOnNext(project -> project.removeProjectVersionIfNotEqual(projectInfoRef.getVersionId()));
  }

  private Mono<IssueRequest> validate(IssueRequest issueRequest) {
    return issueRequest
        .validate()
        .and(validatedProjectInfo(issueRequest))
        .and(validateReporter(issueRequest))
        .thenReturn(issueRequest);
  }

  private Mono<IssueRequest> validatedProjectInfo(IssueRequest issueRequest) {
    return Flux.fromIterable(issueRequest.projects())
        .switchIfEmpty(Mono.error(IssueException::noProjectAttach))
        .flatMap(this::validateProjectInfo)
        .last()
        .thenReturn(issueRequest);
  }

  private Mono<Boolean> validateProjectInfo(ProjectInfo projectInfo) {
    return Mono.just(projectInfo)
        .filter(ProjectInfo::isValid)
        .flatMap(this::isProjectInfoExists)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidProject(projectInfo)));
  }

  private Mono<Boolean> isProjectInfoExists(ProjectInfo projectInfo) {
    return Mono.zip(
            validateProjectId(projectInfo.projectId()),
            validateProjectVersion(projectInfo.projectId(), projectInfo.versionId()),
            Boolean::logicalAnd)
        .map(Boolean::booleanValue);
  }

  private Mono<Boolean> validateProjectId(String projectId) {
    return projectReactiveClient
        .isProjectExists(projectId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }

  private Mono<Boolean> validateProjectVersion(String projectId, String versionId) {
    return projectReactiveClient
        .isProjectVersionExists(projectId, versionId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }

  private Mono<IssueRequest> validateReporter(IssueRequest issueRequest) {
    return fetchUser(issueRequest.reporter()).thenReturn(issueRequest);
  }

  private Mono<User> fetchUser(String userId) {
    return userReactiveClient
        .fetchUser(userId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(IssueException.userServiceException(exception)));
  }
}
