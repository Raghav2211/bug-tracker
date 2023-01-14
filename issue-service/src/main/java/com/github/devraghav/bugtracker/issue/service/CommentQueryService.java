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
import org.springframework.http.codec.ServerSentEvent;
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

  public Flux<ServerSentEvent<Comment>> subscribe(String issueId) {
    var commentAddedStream =
        reactiveMessageBroker.tap(UUID::randomUUID, IssueEvents.CommentAdded.class).stream()
            .filter(commentAdded -> commentAdded.getComment().getIssueId().equals(issueId))
            .map(this::convert);
    var commentUpdatedStream =
        reactiveMessageBroker.tap(UUID::randomUUID, IssueEvents.CommentUpdated.class).stream()
            .filter(commentUpdated -> commentUpdated.getComment().getIssueId().equals(issueId))
            .map(this::convert);
    return Flux.merge(commentAddedStream, commentUpdatedStream);
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

  private ServerSentEvent<Comment> convert(IssueEvents.CommentAdded commentAdded) {
    return ServerSentEvent.<Comment>builder()
        .id(commentAdded.getId().toString())
        .event(commentAdded.getName())
        .data(commentAdded.getComment())
        .build();
  }

  private ServerSentEvent<Comment> convert(IssueEvents.CommentUpdated commentUpdated) {
    return ServerSentEvent.<Comment>builder()
        .id(commentUpdated.getId().toString())
        .event(commentUpdated.getName())
        .data(commentUpdated.getComment())
        .build();
  }
}
