package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.event.internal.CommentAddedEvent;
import com.github.devraghav.bugtracker.issue.event.internal.CommentUpdatedEvent;
import com.github.devraghav.bugtracker.issue.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.issue.event.internal.ReactivePublisher;
import com.github.devraghav.bugtracker.issue.mapper.CommentMapper;
import com.github.devraghav.bugtracker.issue.repository.CommentRepository;
import com.github.devraghav.bugtracker.issue.validation.RequestValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record CommentCommandService(
    RequestValidator requestValidator,
    CommentMapper commentMapper,
    CommentRepository commentRepository,
    UserReactiveClient userReactiveClient,
    ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<Comment> save(CreateCommentRequest createCommentRequest) {
    return requestValidator
        .validate(createCommentRequest)
        .thenReturn(createCommentRequest)
        .map(commentMapper::requestToEntity)
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .flatMap(
            issueComment ->
                eventReactivePublisher
                    .publish(new CommentAddedEvent(createCommentRequest.issueId(), issueComment))
                    .thenReturn(issueComment));
  }

  public Mono<Comment> update(UpdateCommentRequest updateCommentRequest) {
    return requestValidator
        .validate(updateCommentRequest)
        .flatMap(
            validCommentUpdateRequest ->
                findAndUpdateCommentContentById(
                    validCommentUpdateRequest.commentId(), validCommentUpdateRequest.content()))
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .flatMap(
            issueComment ->
                eventReactivePublisher
                    .publish(new CommentUpdatedEvent(updateCommentRequest.issueId(), issueComment))
                    .thenReturn(issueComment));
  }

  private Mono<CommentEntity> findCommentById(String commentId) {
    return commentRepository
        .findById(commentId)
        .switchIfEmpty(Mono.error(() -> CommentException.notFound(commentId)));
  }

  private Mono<CommentEntity> findAndUpdateCommentContentById(String commentId, String content) {
    return findCommentById(commentId)
        .map(commentEntity -> updateIssueCommentEntity(content, commentEntity));
  }

  private CommentEntity updateIssueCommentEntity(String content, CommentEntity commentEntity) {
    commentEntity.setContent(content);
    return commentEntity;
  }

  private Mono<Comment> getComment(CommentEntity commentEntity) {
    return userReactiveClient
        .fetchUser(commentEntity.getUserId())
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(CommentException.userServiceException(exception)))
        .map(
            commentUser -> commentMapper.entityToResponse(commentEntity).user(commentUser).build());
  }
}
