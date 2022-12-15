package com.github.devraghav.issue.service;

import com.github.devraghav.issue.dto.*;
import com.github.devraghav.issue.entity.IssueEntity;
import com.github.devraghav.issue.entity.ProjectInfoRef;
import com.github.devraghav.issue.mapper.IssueMapper;
import com.github.devraghav.issue.repository.IssueRepository;
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
    UserService userService,
    ProjectService projectService,
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
        .and(userService.fetchUser(issueAssignRequest.user()))
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
        .and(userService.fetchUser(issueAssignRequest.user()))
        .thenReturn(issueAssignRequest)
        .flatMap(
            assignRequest -> issueRepository.findAndAddWatcherById(issueId, assignRequest.user()));
  }

  public Mono<Long> removeWatcher(String issueId, IssueAssignRequest issueAssignRequest) {
    return exists(issueId)
        .and(userService.fetchUser(issueAssignRequest.user()))
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
    return userService
        .fetchUser(reporter)
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
        .flatMap(unused -> userService.fetchUser(issueEntity.getReporter()))
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
    return Flux.fromIterable(watchers).flatMap(userService::fetchUser);
  }

  private Mono<User> getAssignee(Optional<String> assignee) {
    return Mono.just(assignee)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(userService::fetchUser);
  }

  private Flux<Project> getProjects(Set<ProjectInfoRef> projectInfoRefs) {
    return Flux.fromIterable(projectInfoRefs).flatMap(this::getProject);
  }

  private Mono<Project> getProject(ProjectInfoRef projectInfoRef) {
    return projectService
        .fetchProject(projectInfoRef.getProjectId())
        .doOnNext(project -> project.removeProjectVersionIfNotEqual(projectInfoRef.getVersionId()));
  }

  private Mono<IssueRequest> validate(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .and(validateHeader(issueRequest))
        .and(validateDescription(issueRequest))
        .and(validatePriority(issueRequest))
        .and(validateSeverity(issueRequest))
        .and(validatedProjectInfo(issueRequest))
        .and(validateReporter(issueRequest))
        .thenReturn(issueRequest);
  }

  private Mono<IssueRequest> validateHeader(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::isHeaderValid)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidSummary(issueRequest.header())));
  }

  private Mono<IssueRequest> validateDescription(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::isDescriptionValid)
        .switchIfEmpty(
            Mono.error(() -> IssueException.invalidDescription(issueRequest.description())));
  }

  private Mono<IssueRequest> validatePriority(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::hasPriority)
        .switchIfEmpty(Mono.error(IssueException::nullPriority))
        .thenReturn(issueRequest);
  }

  private Mono<IssueRequest> validateSeverity(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::hasSeverity)
        .switchIfEmpty(Mono.error(IssueException::nullSeverity))
        .thenReturn(issueRequest);
  }

  private Mono<IssueRequest> validatedProjectInfo(IssueRequest issueRequest) {
    return Flux.fromIterable(issueRequest.projects())
        .switchIfEmpty(Mono.error(IssueException::noProjectAttach))
        .flatMap(this::validateProjectInfo)
        .collectList()
        .thenReturn(issueRequest);
  }

  private Mono<Boolean> validateProjectInfo(ProjectInfo projectInfo) {
    return Mono.just(projectInfo)
        .filter(ProjectInfo::isValid)
        .flatMap(
            validInfo ->
                Mono.zip(
                        validateProjectId(validInfo.projectId()),
                        validateProjectVersion(validInfo.projectId(), validInfo.versionId()),
                        Boolean::logicalAnd)
                    .map(Boolean::booleanValue))
        .switchIfEmpty(Mono.error(() -> IssueException.invalidProject(projectInfo)));
  }

  private Mono<Boolean> validateProjectId(String projectId) {
    return projectService.fetchProject(projectId).map(project -> Boolean.TRUE);
  }

  private Mono<Boolean> validateProjectVersion(String projectId, String versionId) {
    return projectService
        .fetchProjectVersion(projectId, versionId)
        .map(projectVersion -> Boolean.TRUE);
  }

  private Mono<IssueRequest> validateReporter(IssueRequest issueRequest) {
    return Mono.just(issueRequest.reporter())
        .flatMap(userService::fetchUser)
        .thenReturn(issueRequest);
  }
}
