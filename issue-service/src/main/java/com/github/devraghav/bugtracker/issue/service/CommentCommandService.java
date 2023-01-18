package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.mapper.CommentMapper;
import com.github.devraghav.bugtracker.issue.repository.CommentRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record CommentCommandService(
    RequestValidator requestValidator,
    CommentMapper commentMapper,
    CommentRepository commentRepository,
    UserReactiveClient userReactiveClient,
    EventBus.ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<Comment> save(IssueRequest.CreateComment createCommentRequest) {
    // @spotless:off
    return requestValidator
        .validate(createCommentRequest)
        .thenReturn(createCommentRequest)
        .map(commentMapper::requestToEntity)
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .doOnSuccess(comment -> eventReactivePublisher
                    .publish(new IssueEvent.CommentAdded(comment.getIssueId(), comment)));
    // @spotless:on
  }

  public Mono<Comment> update(IssueRequest.UpdateComment updateCommentRequest) {
    // @spotless:off
    return requestValidator
        .validate(updateCommentRequest)
        .flatMap(
            validRequest ->
                findAndUpdateCommentContentById(validRequest.commentId(), validRequest.content()))
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .doOnSuccess(comment -> eventReactivePublisher
                    .publish(new IssueEvent.CommentUpdated(comment.getIssueId(), comment)));
    // @spotless:on
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
    return commentEntity.toBuilder().content(content).lastUpdatedAt(LocalDateTime.now()).build();
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
