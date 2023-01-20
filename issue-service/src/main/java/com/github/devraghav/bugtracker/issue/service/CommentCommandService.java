package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
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
    EventBus.ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<CommentResponse.Comment> save(
      RequestResponse.CreateCommentRequest createCommentRequest) {
    // @spotless:off
    return requestValidator
        .validate(createCommentRequest)
        .thenReturn(createCommentRequest)
        .map(commentMapper::requestToEntity)
        .flatMap(commentRepository::save)
        .map(commentMapper::entityToResponse)
        .doOnSuccess(comment -> eventReactivePublisher.publish(new IssueEvent.CommentAdded(comment.issueId(), comment)));
    // @spotless:on
  }

  public Mono<CommentResponse.Comment> update(
      RequestResponse.UpdateCommentRequest updateCommentRequest) {
    // @spotless:off
    return requestValidator
        .validate(updateCommentRequest)
        .flatMap(validRequest -> findAndUpdateCommentContentById(validRequest.commentId(), validRequest.content()))
        .flatMap(commentRepository::save)
        .map(commentMapper::entityToResponse)
        .doOnSuccess(comment -> eventReactivePublisher.publish(new IssueEvent.CommentUpdated(comment.issueId(), comment)));
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
}
