package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import com.github.devraghav.bugtracker.issue.mapper.CommentMapper;
import com.github.devraghav.bugtracker.issue.repository.CommentRepository;
import com.github.devraghav.bugtracker.issue.request.CommentRequest;
import com.github.devraghav.bugtracker.issue.response.CommentResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record CommentCommandService(
    RequestValidator requestValidator,
    CommentMapper commentMapper,
    CommentRepository commentRepository,
    EventBus.ReactivePublisher<DomainEvent> eventReactivePublisher) {

  public Mono<CommentResponse.Comment> save(CommentRequest.CreateComment createComment) {
    // @spotless:off
    return requestValidator
        .validate(createComment)
        .thenReturn(createComment)
        .map(commentMapper::requestToEntity)
        .flatMap(commentRepository::save)
        .map(commentMapper::entityToResponse)
        .doOnSuccess(comment -> eventReactivePublisher.publish(new IssueEvent.CommentAdded(comment.issueId(), comment)));
    // @spotless:on
  }

  public Mono<CommentResponse.Comment> update(CommentRequest.UpdateComment updateComment) {
    // @spotless:off
    return requestValidator
        .validate(updateComment)
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
