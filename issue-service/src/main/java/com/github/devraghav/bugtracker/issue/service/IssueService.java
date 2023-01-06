package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.entity.ProjectInfoRef;
import com.github.devraghav.bugtracker.issue.event.ReactivePublisher;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueAttachmentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.validation.RequestValidator;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple5;

@Service
public record IssueService(
    RequestValidator requestValidator,
    IssueMapper issueMapper,
    UserReactiveClient userReactiveClient,
    ProjectReactiveClient projectReactiveClient,
    IssueRepository issueRepository,
    IssueAttachmentRepository issueAttachmentRepository,
    IssueCommentFetchService issueCommentFetchService,
    ReactivePublisher<DomainEvent> eventReactivePublisher) {

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

  public Mono<Issue> create(CreateIssueRequest createIssueRequest) {
    return requestValidator
        .validate(createIssueRequest)
        .map(issueMapper::issueRequestToIssueEntity)
        .flatMap(this::save);
  }

  public Mono<Issue> update(String issueId, UpdateIssueRequest request) {
    return findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .map(issueEntity -> issueMapper.issueRequestToIssueEntity(issueEntity, request))
        .flatMap(this::update)
        .switchIfEmpty(Mono.error(() -> IssueException.alreadyEnded(issueId)));
  }

  private Mono<Issue> save(IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(this::generateIssue)
        .flatMap(
            issue ->
                eventReactivePublisher.publish(new IssueCreatedEvent(issue)).thenReturn(issue));
  }

  private Mono<Issue> update(IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(this::generateIssue)
        .flatMap(
            issue ->
                eventReactivePublisher.publish(new IssueUpdatedEvent(issue)).thenReturn(issue));
  }

  public Mono<Boolean> exists(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }

  public Mono<Void> assignee(String issueId, IssueAssignRequest issueAssignRequest) {
    var issueMono = exists(issueId).map(unused -> issueId);
    if (issueAssignRequest.user() == null) {
      return unassigned(issueMono);
    }
    var userMono = fetchUser(issueAssignRequest.user());
    var issueUserMono = Mono.zip(issueMono, userMono);
    return assignee(issueUserMono);
  }

  private Mono<Void> assignee(Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> assignee(tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> assignee(String issueId, User user) {
    return issueRepository
        .findAndSetAssigneeById(issueId, user.id())
        .flatMap(unused -> eventReactivePublisher.publish(new IssueAssignedEvent(issueId, user)));
  }

  private Mono<Void> unassigned(Mono<String> issueMono) {
    return issueMono.flatMap(this::unassigned).then();
  }

  private Mono<Void> unassigned(String issueId) {
    return issueRepository
        .findAndUnSetAssigneeById(issueId)
        .flatMap(unused -> eventReactivePublisher.publish(new IssueUnassignedEvent(issueId)));
  }

  public Mono<Void> watch(String issueId, IssueAssignRequest issueAssignRequest, boolean watch) {
    var issueMono = exists(issueId).map(unused -> issueId);
    var userMono = fetchUser(issueAssignRequest.user());
    var issueUserMono = Mono.zip(issueMono, userMono);
    return watch ? watch(issueUserMono) : unwatch(issueUserMono);
  }

  private Mono<Void> watch(Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> watch(tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> watch(String issueId, User user) {
    return issueRepository
        .findAndAddWatcherById(issueId, user.id())
        .flatMap(
            unused -> eventReactivePublisher.publish(new IssueWatchStartedEvent(issueId, user)));
  }

  private Mono<Void> unwatch(Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> unwatch(tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> unwatch(String issueId, User user) {
    return issueRepository
        .findAndPullWatcherById(issueId, user.id())
        .flatMap(unused -> eventReactivePublisher.publish(new IssueWatchEndedEvent(issueId, user)));
  }

  public Mono<Void> resolve(String issueId) {
    var resolveTime = LocalDateTime.now();
    return issueRepository
        .findAndSetEndedAtById(issueId, resolveTime.toEpochSecond(ZoneOffset.UTC))
        .flatMap(unused -> eventReactivePublisher.publish(new IssueResolvedEvent(issueId)))
        .then();
  }

  public Mono<String> uploadAttachment(String issueId, FilePart filePart) {
    return issueAttachmentRepository.upload(issueId, filePart.filename(), filePart.content());
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
    var watchersMono = getWatchers(issueEntity.getWatchers()).collect(Collectors.toSet());
    var commentsMono = issueCommentFetchService.getComments(issueEntity.getId()).collectList();
    var projectsMono = getProjects(issueEntity.getProjects()).collectList();
    var reporterMono = fetchUser(issueEntity.getReporter());
    var assigneeMono =
        getAssignee(issueEntity.getAssignee()).switchIfEmpty(Mono.just(Optional.empty()));
    return Mono.zip(watchersMono, commentsMono, projectsMono, assigneeMono, reporterMono)
        .map(tuple5 -> generateIssue(issueEntity, tuple5));
  }

  private Issue generateIssue(
      IssueEntity issueEntity,
      Tuple5<Set<User>, List<IssueComment>, List<Project>, Optional<User>, User> tuple5) {
    var issueBuilder =
        issueMapper
            .issueEntityToIssue(issueEntity)
            .watchers(tuple5.getT1())
            .comments(tuple5.getT2())
            .projects(tuple5.getT3())
            .reporter(tuple5.getT5())
            .endedAt(issueEntity.getEndedAt());
    tuple5.getT4().ifPresent(issueBuilder::assignee);
    return issueBuilder.build();
  }

  private Flux<User> getWatchers(Set<String> watchers) {
    return Flux.fromIterable(watchers).flatMap(this::fetchUser);
  }

  private Mono<Optional<User>> getAssignee(Optional<String> assignee) {
    return Mono.just(assignee)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .flatMap(this::fetchUser)
        .map(Optional::of);
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

  private Mono<Boolean> validateProjectId(String projectId) {
    return projectReactiveClient
        .isProjectExists(projectId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }

  private Mono<User> fetchUser(String userId) {
    return userReactiveClient
        .fetchUser(userId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(IssueException.userServiceException(exception)));
  }
}
