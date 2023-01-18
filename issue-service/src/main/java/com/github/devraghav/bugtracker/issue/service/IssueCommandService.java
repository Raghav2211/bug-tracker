package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueAttachmentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueCommandService {

  private final RequestValidator requestValidator;
  private final IssueMapper issueMapper;
  private final IssueQueryService issueQueryService;
  private final IssueRepository issueRepository;
  private final IssueAttachmentRepository issueAttachmentRepository;
  private final EventBus.ReactivePublisher<DomainEvent> domainEventPublisher;

  public Mono<Issue> create(String userId, IssueRequest.Create createIssueRequest) {
    return requestValidator
        .validate(userId, createIssueRequest)
        .map(issueMapper::issueRequestToIssueEntity)
        .flatMap(this::save);
  }

  public Mono<Issue> update(String userId, String issueId, IssueRequest.Update request) {
    return issueQueryService
        .findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .map(issueEntity -> issueMapper.issueRequestToIssueEntity(issueEntity, request))
        .flatMap(issueEntity -> update(userId, issueEntity))
        .switchIfEmpty(Mono.error(() -> IssueException.alreadyEnded(issueId)));
  }

  public Mono<Void> monitor(String issueId, IssueRequest.Assign assignRequest) {
    log.info("monitor {} with assignRequest {}", assignRequest.monitorType(), assignRequest);
    var issueMono = issueQueryService.exists(issueId).map(unused -> issueId);
    if (MonitorType.UNASSIGN == assignRequest.monitorType()) {
      return unassigned(issueMono, assignRequest.requestedBy());
    } else {
      var userMono = issueQueryService.fetchUser(assignRequest.user());
      var issueUserMono = Mono.zip(issueMono, userMono);
      if (MonitorType.ASSIGN == assignRequest.monitorType()) {
        return assignee(issueUserMono, assignRequest.requestedBy());
      } else if (MonitorType.WATCH == assignRequest.monitorType()) {
        return watch(issueUserMono, assignRequest.requestedBy());
      } else {
        return unwatch(issueUserMono, assignRequest.requestedBy());
      }
    }
  }

  private Mono<Void> assignee(Mono<Tuple2<String, User>> issueUserMono, String requestedBy) {
    return issueUserMono.flatMap(tuple2 -> assignee(tuple2.getT1(), tuple2.getT2(), requestedBy));
  }

  private Mono<Void> assignee(String issueId, User user, String requestedBy) {
    return issueRepository
        .findAndSetAssigneeById(issueId, user.id())
        .doOnSuccess(
            unused ->
                domainEventPublisher.publish(new IssueEvent.Assigned(issueId, user, requestedBy)))
        .then();
  }

  private Mono<Void> unassigned(Mono<String> issueMono, String requestedBy) {
    return issueMono.flatMap(issueId -> unassigned(issueId, requestedBy)).then();
  }

  private Mono<Void> unassigned(String issueId, String requestedBy) {
    return issueRepository
        .findAndUnSetAssigneeById(issueId)
        .doOnSuccess(
            unused -> domainEventPublisher.publish(new IssueEvent.Unassigned(issueId, requestedBy)))
        .then();
  }

  private Mono<Void> watch(Mono<Tuple2<String, User>> issueUserMono, String requestedBy) {
    return issueUserMono.flatMap(tuple2 -> watch(tuple2.getT1(), tuple2.getT2(), requestedBy));
  }

  private Mono<Void> watch(String issueId, User user, String requestedBy) {
    log.info("Watch by issueId {} and user {}", issueId, user);
    return issueRepository
        .findAndAddWatcherById(issueId, user.id())
        .doOnSuccess(
            unused ->
                domainEventPublisher.publish(
                    new IssueEvent.WatchStarted(issueId, user, requestedBy)))
        .then();
  }

  private Mono<Void> unwatch(Mono<Tuple2<String, User>> issueUserMono, String requestedBy) {
    return issueUserMono.flatMap(tuple2 -> unwatch(tuple2.getT1(), tuple2.getT2(), requestedBy));
  }

  private Mono<Void> unwatch(String issueId, User user, String requestedBy) {
    return issueRepository
        .findAndPullWatcherById(issueId, user.id())
        .doOnSuccess(
            unused ->
                domainEventPublisher.publish(new IssueEvent.WatchEnded(issueId, user, requestedBy)))
        .then();
  }

  public Mono<Void> resolve(String issueId, String resolvedBy) {
    var resolveTime = LocalDateTime.now();
    // @spotless:off
    return issueRepository
        .findAndSetEndedAtById(issueId, resolveTime)
        .doOnSuccess(unused ->
                domainEventPublisher.publish(new IssueEvent.Resolved(issueId, resolveTime, resolvedBy)))
        .then();
    // @spotless:on
  }

  public Mono<String> uploadAttachment(String issueId, FilePart filePart) {
    return issueAttachmentRepository.upload(issueId, filePart.filename(), filePart.content());
  }

  private Mono<Issue> save(IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(issueQueryService::generateIssue)
        .doOnSuccess(issue -> domainEventPublisher.publish(new IssueEvent.Created(issue)));
  }

  private Mono<Issue> update(String userId, IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(issueQueryService::generateIssue)
        .doOnSuccess(issue -> domainEventPublisher.publish(new IssueEvent.Updated(userId, issue)));
  }
}
