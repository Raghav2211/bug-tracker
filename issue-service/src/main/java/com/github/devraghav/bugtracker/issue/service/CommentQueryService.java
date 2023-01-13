package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.dto.CommentException;
import com.github.devraghav.bugtracker.issue.dto.UserClientException;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.event.internal.IssueEvents;
import com.github.devraghav.bugtracker.issue.mapper.CommentMapper;
import com.github.devraghav.bugtracker.issue.repository.CommentRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record CommentQueryService(
    CommentMapper commentMapper,
    UserReactiveClient userReactiveClient,
    CommentRepository commentRepository,
    EventBus.ReactiveMessageBroker reactiveMessageBroker) {

  public Flux<Comment> getComments(String issueId) {
    return commentRepository.findAllByIssueId(issueId).flatMap(this::getComment);
  }

  public Mono<Comment> getComment(String id) {
    return commentRepository
        .findById(id)
        .flatMap(this::getComment)
        .switchIfEmpty(Mono.error(() -> CommentException.notFound(id)));
  }

  public Flux<Comment> subscribe(String issueId) {
    var commentAddedStream =
        reactiveMessageBroker
            .tap(UUID::randomUUID, IssueEvents.CommentAdded.class)
            .map(IssueEvents.CommentAdded::getComment);
    var commentUpdatedStream =
        reactiveMessageBroker
            .tap(UUID::randomUUID, IssueEvents.CommentUpdated.class)
            .map(IssueEvents.CommentUpdated::getComment);
    var commentStream = Flux.merge(commentAddedStream, commentUpdatedStream);
    return commentStream.filter(comment -> comment.getIssueId().equals(issueId));
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
