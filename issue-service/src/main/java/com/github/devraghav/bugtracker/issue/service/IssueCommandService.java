package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueAttachmentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.validation.RequestValidator;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@Slf4j
public record IssueCommandService(
    RequestValidator requestValidator,
    IssueMapper issueMapper,
    IssueQueryService issueQueryService,
    IssueRepository issueRepository,
    IssueAttachmentRepository issueAttachmentRepository,
    EventBus.ReactivePublisher domainEventPublisher) {

  public Mono<Issue> create(IssueRequests.Create createIssueRequest) {
    return requestValidator
        .validate(createIssueRequest)
        .map(issueMapper::issueRequestToIssueEntity)
        .flatMap(this::save);
  }

  public Mono<Issue> update(String issueId, IssueRequests.Update request) {
    return issueQueryService
        .findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .map(issueEntity -> issueMapper.issueRequestToIssueEntity(issueEntity, request))
        .flatMap(this::update)
        .switchIfEmpty(Mono.error(() -> IssueException.alreadyEnded(issueId)));
  }

  public Mono<Void> monitor(String issueId, IssueRequests.Assign assignRequest) {
    log.info("monitor {} with assignRequest {}", assignRequest.monitorType(), assignRequest);
    var issueMono = issueQueryService.exists(issueId).map(unused -> issueId);
    if (MonitorType.UNASSIGN == assignRequest.monitorType()) {
      return unassigned(issueMono);
    } else {
      var userMono = issueQueryService.fetchUser(assignRequest.user());
      var issueUserMono = Mono.zip(issueMono, userMono);
      if (MonitorType.ASSIGN == assignRequest.monitorType()) {
        return assignee(issueUserMono);
      } else if (MonitorType.WATCH == assignRequest.monitorType()) {
        return watch(issueUserMono);
      } else {
        return unwatch(issueUserMono);
      }
    }
  }

  private Mono<Void> assignee(Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> assignee(tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> assignee(String issueId, User user) {
    return issueRepository
        .findAndSetAssigneeById(issueId, user.id())
        .flatMap(unused -> domainEventPublisher.publish(new IssueEvents.Assigned(issueId, user)));
  }

  private Mono<Void> unassigned(Mono<String> issueMono) {
    return issueMono.flatMap(this::unassigned).then();
  }

  private Mono<Void> unassigned(String issueId) {
    return issueRepository
        .findAndUnSetAssigneeById(issueId)
        .flatMap(unused -> domainEventPublisher.publish(new IssueEvents.Unassigned(issueId)));
  }

  private Mono<Void> watch(Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> watch(tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> watch(String issueId, User user) {
    log.info("Watch by issueId {} and user {}", issueId, user);
    return issueRepository
        .findAndAddWatcherById(issueId, user.id())
        .flatMap(
            unused -> domainEventPublisher.publish(new IssueEvents.WatchStarted(issueId, user)));
  }

  private Mono<Void> unwatch(Mono<Tuple2<String, User>> issueUserMono) {
    return issueUserMono.flatMap(tuple2 -> unwatch(tuple2.getT1(), tuple2.getT2()));
  }

  private Mono<Void> unwatch(String issueId, User user) {
    return issueRepository
        .findAndPullWatcherById(issueId, user.id())
        .flatMap(unused -> domainEventPublisher.publish(new IssueEvents.WatchEnded(issueId, user)));
  }

  public Mono<Void> resolve(String issueId) {
    var resolveTime = LocalDateTime.now();
    return issueRepository
        .findAndSetEndedAtById(issueId, resolveTime)
        .flatMap(
            unused -> domainEventPublisher.publish(new IssueEvents.Resolved(issueId, resolveTime)))
        .then();
  }

  public Mono<String> uploadAttachment(String issueId, FilePart filePart) {
    return issueAttachmentRepository.upload(issueId, filePart.filename(), filePart.content());
  }

  private Mono<Issue> save(IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(issueQueryService::generateIssue)
        .flatMap(
            issue ->
                domainEventPublisher.publish(new IssueEvents.Created(issue)).thenReturn(issue));
  }

  private Mono<Issue> update(IssueEntity issueEntity) {
    return issueRepository
        .save(issueEntity)
        .flatMap(issueQueryService::generateIssue)
        .flatMap(
            issue ->
                domainEventPublisher.publish(new IssueEvents.Updated(issue)).thenReturn(issue));
  }
}
