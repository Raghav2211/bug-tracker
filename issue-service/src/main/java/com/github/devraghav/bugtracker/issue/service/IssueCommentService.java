package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import com.github.devraghav.bugtracker.issue.event.ReactivePublisher;
import com.github.devraghav.bugtracker.issue.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.issue.event.internal.IssueCommentAddedEvent;
import com.github.devraghav.bugtracker.issue.event.internal.IssueCommentUpdatedEvent;
import com.github.devraghav.bugtracker.issue.mapper.IssueCommentMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueCommentRepository;
import com.github.devraghav.bugtracker.issue.validation.RequestValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record IssueCommentService(
    RequestValidator requestValidator,
    IssueCommentMapper issueCommentMapper,
    IssueCommentFetchService issueCommentFetchService,
    IssueCommentRepository issueCommentRepository,
    ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<IssueComment> save(CreateCommentRequest createCommentRequest) {
    return requestValidator
        .validate(createCommentRequest)
        .thenReturn(createCommentRequest)
        .map(issueCommentMapper::requestToEntity)
        .flatMap(issueCommentRepository::save)
        .flatMap(issueCommentFetchService::getComment)
        .flatMap(
            issueComment ->
                eventReactivePublisher
                    .publish(
                        new IssueCommentAddedEvent(createCommentRequest.issueId(), issueComment))
                    .thenReturn(issueComment));
  }

  public Mono<IssueComment> update(UpdateCommentRequest updateCommentRequest) {
    return requestValidator
        .validate(updateCommentRequest)
        .flatMap(
            validCommentUpdateRequest ->
                findAndUpdateCommentContentById(
                    validCommentUpdateRequest.commentId(), validCommentUpdateRequest.content()))
        .flatMap(issueCommentRepository::save)
        .flatMap(issueCommentFetchService::getComment)
        .flatMap(
            issueComment ->
                eventReactivePublisher
                    .publish(
                        new IssueCommentUpdatedEvent(updateCommentRequest.issueId(), issueComment))
                    .thenReturn(issueComment));
  }

  private Mono<IssueCommentEntity> findCommentById(String commentId) {
    return issueCommentRepository
        .findById(commentId)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidComment(commentId)));
  }

  private Mono<IssueCommentEntity> findAndUpdateCommentContentById(
      String commentId, String content) {
    return findCommentById(commentId)
        .map(commentEntity -> updateIssueCommentEntity(content, commentEntity));
  }

  private IssueCommentEntity updateIssueCommentEntity(
      String content, IssueCommentEntity issueCommentEntity) {
    issueCommentEntity.setContent(content);
    return issueCommentEntity;
  }
}
