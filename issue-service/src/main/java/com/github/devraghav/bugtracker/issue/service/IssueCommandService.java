package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.event.internal.ReactivePublisher;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueAttachmentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.validation.RequestValidator;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public record IssueCommandService(
    RequestValidator requestValidator,
    IssueMapper issueMapper,
    IssueQueryService issueQueryService,
    IssueRepository issueRepository,
    IssueAttachmentRepository issueAttachmentRepository,
    ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<Issue> create(CreateIssueRequest createIssueRequest) {
    return requestValidator
        .validate(createIssueRequest)
        .map(issueMapper::issueRequestToIssueEntity)
        .flatMap(this::save);
  }

  public Mono<Issue> update(String issueId, UpdateIssueRequest request) {
    return issueQueryService
        .findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .map(issueEntity -> issueMapper.issueRequestToIssueEntity(issueEntity, request))
        .flatMap(this::update)
        .switchIfEmpty(Mono.error(() -> IssueException.alreadyEnded(issueId)));
  }

  private Mono<Issue> save(IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(issueQueryService::generateIssue)
        .flatMap(
            issue ->
                eventReactivePublisher.publish(new IssueCreatedEvent(issue)).thenReturn(issue));
  }

  private Mono<Issue> update(IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(issueQueryService::generateIssue)
        .flatMap(
            issue ->
                eventReactivePublisher.publish(new IssueUpdatedEvent(issue)).thenReturn(issue));
  }

  public Mono<Void> assignee(String issueId, AssignRequest assignRequest) {
    var issueMono = issueQueryService.exists(issueId).map(unused -> issueId);
    if (assignRequest.user() == null) {
      return unassigned(issueMono);
    }
    var userMono = issueQueryService.fetchUser(assignRequest.user());
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

  public Mono<Void> watch(String issueId, AssignRequest assignRequest, boolean watch) {
    var issueMono = issueQueryService.exists(issueId).map(unused -> issueId);
    var userMono = issueQueryService.fetchUser(assignRequest.user());
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
}
