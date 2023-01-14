package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.mapper.CommentMapper;
import com.github.devraghav.bugtracker.issue.repository.CommentRepository;
import com.github.devraghav.bugtracker.issue.validation.RequestValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CommentCommandService {

  private final RequestValidator requestValidator;
  private final CommentMapper commentMapper;
  private final CommentRepository commentRepository;
  private final UserReactiveClient userReactiveClient;
  private final EventBus.ReactivePublisher<DomainEvent> eventReactivePublisher;

  public CommentCommandService(
      RequestValidator requestValidator,
      CommentMapper commentMapper,
      CommentRepository commentRepository,
      UserReactiveClient userReactiveClient,
      DomainEventPublisher eventReactivePublisher) {
    this.requestValidator = requestValidator;
    this.commentMapper = commentMapper;
    this.commentRepository = commentRepository;
    this.userReactiveClient = userReactiveClient;
    this.eventReactivePublisher = eventReactivePublisher;
  }

  public Mono<Comment> save(IssueRequests.CreateComment createCommentRequest) {
    return requestValidator
        .validate(createCommentRequest)
        .thenReturn(createCommentRequest)
        .map(commentMapper::requestToEntity)
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .doOnSuccess(
            comment ->
                eventReactivePublisher.publish(
                    new IssueEvents.CommentAdded(comment.getIssueId(), comment)));
  }

  public Mono<Comment> update(IssueRequests.UpdateComment updateCommentRequest) {
    return requestValidator
        .validate(updateCommentRequest)
        .flatMap(
            validRequest ->
                findAndUpdateCommentContentById(validRequest.commentId(), validRequest.content()))
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .doOnSuccess(
            comment ->
                eventReactivePublisher.publish(
                    new IssueEvents.CommentUpdated(comment.getIssueId(), comment)));
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
