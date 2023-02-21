package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueAttachmentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import com.github.devraghav.bugtracker.issue.user.UserClient;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueCommandService {

  private final RequestValidator requestValidator;
  private final IssueMapper issueMapper;
  private final IssueQueryService issueQueryService;
  private final UserClient userClient;
  private final IssueRepository issueRepository;
  private final IssueAttachmentRepository issueAttachmentRepository;
  private final EventBus.ReactivePublisher<DomainEvent> domainEventPublisher;

  public Mono<IssueResponse.Issue> create(String userId, IssueRequest.CreateIssue createIssue) {
    // @spotless:off
    return requestValidator
        .validate(createIssue)
        .map(validateRequest -> issueMapper.issueRequestToIssueEntity(userId,
                validateRequest.getT1(), validateRequest.getT2()))
        .flatMap(issueEntity ->
                upsert(issueEntity, issue -> domainEventPublisher.publish(new IssueEvent.Created(issue))));
    // @spotless:on
  }

  public Mono<IssueResponse.Issue> update(
      String userId, String issueId, IssueRequest.UpdateIssue updateRequest) {
    // @spotless:off
    return issueQueryService
        .findById(issueId)
        .filter(issueEntity -> Objects.nonNull(issueEntity.getEndedAt()))
        .map(issueEntity -> issueMapper.issueRequestToIssueEntity(userId, issueEntity, updateRequest))
        .flatMap(issueEntity ->
                upsert(issueEntity, issue -> domainEventPublisher.publish(new IssueEvent.Updated(userId, issue))))
        .switchIfEmpty(Mono.error(() -> IssueException.alreadyEnded(issueId)));
    // @spotless:on
  }

  public Mono<Void> monitor(IssueRequest.Monitor monitor) {
    log.info("monitor {} with assignRequest {}", monitor.monitorType(), monitor);
    var issueMono = issueQueryService.exists(monitor.issueId()).map(unused -> monitor.issueId());
    if (IssueRequest.MonitorType.UNASSIGN == monitor.monitorType()) {
      return unassigned(issueMono, monitor.requestBy());
    } else {
      if (IssueRequest.MonitorType.ASSIGN == monitor.monitorType()) {
        return assignee(issueMono, monitor.user(), monitor.requestBy());
      } else if (IssueRequest.MonitorType.WATCH == monitor.monitorType()) {
        return watch(issueMono, monitor.user(), monitor.requestBy());
      } else {
        return unwatch(issueMono, monitor.user(), monitor.requestBy());
      }
    }
  }

  private Mono<Void> assignee(Mono<String> issueMono, String userId, String requestBy) {
    return issueMono.flatMap(issueId -> assignee(issueId, userId, requestBy));
  }

  private Mono<Void> assignee(String issueId, String userId, String requestBy) {
    // @spotless:off
    return issueRepository
        .findAndSetAssigneeById(issueId, userId)
        .doOnSuccess(unused ->
                domainEventPublisher.publish(new IssueEvent.Assigned(issueId, userId, requestBy)))
        .then();
    // @spotless:on
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

  private Mono<Void> watch(Mono<String> issueMono, String userId, String requestedBy) {
    return issueMono.flatMap(issueId -> watch(issueId, userId, requestedBy));
  }

  private Mono<Void> watch(String issueId, String userId, String requestedBy) {
    // @spotless:off
    return issueRepository
        .findAndAddWatcherById(issueId, userId)
        .doOnSuccess(unused ->
                domainEventPublisher.publish(new IssueEvent.WatchStarted(issueId, userId, requestedBy)))
        .then();
    // @spotless:on
  }

  private Mono<Void> unwatch(Mono<String> issueMono, String userId, String requestedBy) {
    return issueMono.flatMap(issueId -> unwatch(issueId, userId, requestedBy));
  }

  private Mono<Void> unwatch(String issueId, String userId, String requestedBy) {
    // @spotless:off
    return issueRepository
        .findAndPullWatcherById(issueId, userId)
        .doOnSuccess(unused -> domainEventPublisher.publish(new IssueEvent.WatchEnded(issueId, userId, requestedBy)))
        .then();
    // @spotless:on
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

  private Mono<IssueResponse.Issue> upsert(
      IssueEntity issueEntity, Consumer<IssueResponse.Issue> onSuccessConsumer) {
    return issueRepository
        .save(issueEntity)
        .map(issueMapper::issueEntityToIssue)
        .doOnSuccess(onSuccessConsumer);
  }
}
