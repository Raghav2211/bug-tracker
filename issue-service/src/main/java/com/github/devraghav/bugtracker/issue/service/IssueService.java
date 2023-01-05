package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.entity.ProjectInfoRef;
import com.github.devraghav.bugtracker.issue.kafka.producer.KafkaProducer;
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
    KafkaProducer kafkaProducer) {

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

  public Mono<Issue> create(String requestId, CreateIssueRequest createIssueRequest) {
    return requestValidator
        .validate(createIssueRequest)
        .flatMap(validRequest -> kafkaProducer.sendIssueCreateCommand(requestId, validRequest))
        .map(issueMapper::issueRequestToIssueEntity)
        .flatMap(issueEntity -> save(requestId, issueEntity));
  }

  public Mono<Issue> update(String requestId, String issueId, UpdateIssueRequest request) {
    return findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .flatMap(
            issueEntity ->
                kafkaProducer.sendIssueUpdateCommand(requestId, request).thenReturn(issueEntity))
        .map(issueEntity -> issueMapper.issueRequestToIssueEntity(issueEntity, request))
        .flatMap(issueEntity -> update(requestId, issueEntity))
        .switchIfEmpty(Mono.error(() -> IssueException.alreadyEnded(issueId)));
  }

  private Mono<Issue> save(String requestId, IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(this::generateIssue)
        .flatMap(issue -> kafkaProducer.sendIssueCreatedEvent(requestId, issue));
  }

  private Mono<Issue> update(String requestId, IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(this::generateIssue)
        .flatMap(issue -> kafkaProducer.sendIssueUpdatedEvent(requestId, issue));
  }

  public Mono<Boolean> exists(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }

  public Mono<Void> assignee(
      String requestId, String issueId, IssueAssignRequest issueAssignRequest) {
    var issueMono = exists(issueId).map(unused -> issueId);
    if (issueAssignRequest.user() == null) {
      return unassigned(requestId, issueMono);
    }
    var userMono = fetchUser(issueAssignRequest.user());
    var issueUserMono = Mono.zip(issueMono, userMono);
    return assignee(requestId, issueUserMono);
  }

  private Mono<Void> assignee(String requestId, Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> assignee(requestId, tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> assignee(String requestId, String issueId, User user) {
    return issueRepository
        .findAndSetAssigneeById(issueId, user.id())
        .flatMap(unused -> kafkaProducer.sendIssueAssignedEvent(requestId, issueId, user))
        .then();
  }

  private Mono<Void> unassigned(String requestId, Mono<String> issueMono) {
    return issueMono.flatMap(issueId -> unassigned(requestId, issueId)).then();
  }

  private Mono<Void> unassigned(String requestId, String issueId) {
    return issueRepository
        .findAndUnSetAssigneeById(issueId)
        .flatMap(unused -> kafkaProducer.sendIssueUnassignedEvent(requestId, issueId))
        .then();
  }

  public Mono<Void> watch(
      String requestId, String issueId, IssueAssignRequest issueAssignRequest, boolean watch) {
    var issueMono = exists(issueId).map(unused -> issueId);
    var userMono = fetchUser(issueAssignRequest.user());
    var issueUserMono = Mono.zip(issueMono, userMono);
    return watch ? watch(requestId, issueUserMono) : unwatch(requestId, issueUserMono);
  }

  private Mono<Void> watch(String requestId, Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> watch(requestId, tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> watch(String requestId, String issueId, User user) {
    return issueRepository
        .findAndAddWatcherById(issueId, user.id())
        .flatMap(unused -> kafkaProducer.sendIssueWatchedEvent(requestId, issueId, user))
        .then();
  }

  private Mono<Void> unwatch(String requestId, Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> unwatch(requestId, tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> unwatch(String requestId, String issuedId, User user) {
    return issueRepository
        .findAndPullWatcherById(issuedId, user.id())
        .flatMap(unused -> kafkaProducer.sendIssueUnwatchedEvent(requestId, issuedId, user))
        .then();
  }

  public Mono<Void> resolve(String requestId, String issueId) {
    var resolveTime = LocalDateTime.now();
    return issueRepository
        .findAndSetEndedAtById(issueId, resolveTime.toEpochSecond(ZoneOffset.UTC))
        .flatMap(unused -> kafkaProducer.sendIssueResolvedEvent(requestId, issueId, resolveTime))
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
