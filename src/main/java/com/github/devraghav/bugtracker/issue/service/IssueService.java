package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.entity.ProjectInfoRef;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.ProjectException;
import com.github.devraghav.bugtracker.project.service.ProjectService;
import com.github.devraghav.bugtracker.user.dto.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IssueService {
  private final String userFindByIdURL;
  private final WebClient webClient;
  private final IssueRepository issueRepository;
  private final IssueCommentFetchService issueCommentFetchService;
  private final ProjectService projectService;

  public IssueService(
      @Value("${app.external.user-service.url}") String userServiceURL,
      WebClient webClient,
      IssueRepository issueRepository,
      IssueCommentFetchService issueCommentFetchService,
      ProjectService projectService) {
    this.webClient = webClient;
    this.issueRepository = issueRepository;
    this.issueCommentFetchService = issueCommentFetchService;
    this.projectService = projectService;
    this.userFindByIdURL = userServiceURL + "/api/rest/v1/user/{id}";
  }

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
    return validate(issueRequest).map(IssueEntity::new).flatMap(this::save);
  }

  public Mono<Issue> update(String issueId, IssueUpdateRequest request) {
    return findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .map(issueEntity -> new IssueEntity(issueEntity, request))
        .flatMap(this::save)
        .switchIfEmpty(Mono.error(() -> IssueException.cannotUpdateIssueEnd(issueId)));
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
        .and(getUser(issueAssignRequest.getUser()))
        .thenReturn(issueAssignRequest)
        .flatMap(
            assignRequest ->
                issueRepository.findAndSetAssigneeById(issueId, assignRequest.getUser()));
  }

  public Mono<Long> unassigned(String issueId) {
    return exists(issueId)
        .flatMap(assignRequest -> issueRepository.findAndUnSetAssigneeById(issueId));
  }

  public Mono<Long> addWatcher(String issueId, IssueAssignRequest issueAssignRequest) {
    return exists(issueId)
        .and(getUser(issueAssignRequest.getUser()))
        .thenReturn(issueAssignRequest)
        .flatMap(
            assignRequest ->
                issueRepository.findAndAddWatcherById(issueId, assignRequest.getUser()));
  }

  public Mono<Long> removeWatcher(String issueId, IssueAssignRequest issueAssignRequest) {
    return exists(issueId)
        .and(getUser(issueAssignRequest.getUser()))
        .thenReturn(issueAssignRequest)
        .flatMap(
            assignRequest ->
                issueRepository.findAndPullWatcherById(issueId, assignRequest.getUser()));
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
    return getUser(reporter)
        .flatMapMany(
            unused -> issueRepository.findAllByReporter(reporter).flatMap(this::generateIssue));
  }

  private Mono<Issue> generateIssue(IssueEntity issueEntity) {
    var issueBuilder = Issue.builder(issueEntity);
    return getWatchers(issueEntity.getWatchers())
        .collect(Collectors.toSet())
        .map(issueBuilder::watchers)
        .flatMapMany(unused -> issueCommentFetchService.getComments(issueEntity.getId()))
        .collectList()
        .map(issueBuilder::comments)
        .flatMap(unused -> getUser(issueEntity.getReporter()))
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
    return Flux.fromIterable(watchers).flatMap(this::getUser);
  }

  private Mono<User> getAssignee(Optional<String> assignee) {
    return Mono.just(assignee)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(this::getUser)
        .switchIfEmpty(Mono.just(new User()));
  }

  private Flux<Project> getProjects(Set<ProjectInfoRef> projectInfoRefs) {
    return Flux.fromIterable(projectInfoRefs).flatMap(this::getProject);
  }

  private Mono<Project> getProject(ProjectInfoRef projectInfoRef) {
    return projectService
        .findById(projectInfoRef.getProjectId())
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
        .switchIfEmpty(Mono.error(() -> IssueException.invalidSummary(issueRequest.getHeader())));
  }

  private Mono<IssueRequest> validateDescription(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::isDescriptionValid)
        .switchIfEmpty(
            Mono.error(() -> IssueException.invalidDescription(issueRequest.getDescription())));
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
    return Flux.fromIterable(issueRequest.getProjects())
        .switchIfEmpty(Mono.error(ProjectException::noProjectInfoFound))
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
                        validateProjectId(validInfo.getProjectId()),
                        validateProjectVersion(validInfo.getProjectId(), validInfo.getVersionId()),
                        Boolean::logicalAnd)
                    .map(Boolean::booleanValue))
        .switchIfEmpty(Mono.error(() -> ProjectException.invalidProjectInfo(projectInfo)));
  }

  private Mono<Boolean> validateProjectId(String projectId) {
    return projectService
        .exists(projectId)
        .onErrorResume(
            ProjectException.class,
            exception -> Mono.error(() -> IssueException.invalidProject(projectId)));
  }

  private Mono<Boolean> validateProjectVersion(String projectId, String versionId) {
    return projectService
        .existsByIdAndVersionId(projectId, versionId)
        .onErrorResume(
            ProjectException.class,
            exception ->
                Mono.error(() -> IssueException.invalidProjectVersion(projectId, versionId)));
  }

  private Mono<IssueRequest> validateReporter(IssueRequest issueRequest) {
    return Mono.just(issueRequest.getReporter()).flatMap(this::getUser).thenReturn(issueRequest);
  }

  public Mono<User> getUser(String userId) {
    return webClient
        .get()
        .uri(userFindByIdURL, userId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(IssueException.invalidUser(userId)))
        .bodyToMono(User.class);
  }
}
