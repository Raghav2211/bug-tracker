package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.dto.CommentRequestResponse;
import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
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
    CommentRepository commentRepository,
    EventBus.ReactiveMessageBroker reactiveMessageBroker) {

  public Flux<CommentRequestResponse.CommentResponse> getComments(String issueId) {
    return commentRepository.findAllByIssueId(issueId).map(commentMapper::entityToResponse);
  }

  public Mono<CommentRequestResponse.CommentResponse> getComment(String id) {
    return commentRepository
        .findById(id)
        .map(commentMapper::entityToResponse)
        .switchIfEmpty(Mono.error(() -> CommentException.notFound(id)));
  }

  public Flux<ServerSentEvent<CommentRequestResponse.CommentResponse>> subscribe(String issueId) {
    var commentAddedStream =
        reactiveMessageBroker.tap(UUID::randomUUID, IssueEvent.CommentAdded.class).stream()
            .filter(commentAdded -> commentAdded.getComment().issueId().equals(issueId))
            .map(this::convert);
    var commentUpdatedStream =
        reactiveMessageBroker.tap(UUID::randomUUID, IssueEvent.CommentUpdated.class).stream()
            .filter(commentUpdated -> commentUpdated.getComment().issueId().equals(issueId))
            .map(this::convert);
    return Flux.merge(commentAddedStream, commentUpdatedStream);
  }

  private ServerSentEvent<CommentRequestResponse.CommentResponse> convert(
      IssueEvent.CommentAdded commentAdded) {
    return ServerSentEvent.<CommentRequestResponse.CommentResponse>builder()
        .id(commentAdded.getId().toString())
        .event(commentAdded.getName())
        .data(commentAdded.getComment())
        .build();
  }

  private ServerSentEvent<CommentRequestResponse.CommentResponse> convert(
      IssueEvent.CommentUpdated commentUpdated) {
    return ServerSentEvent.<CommentRequestResponse.CommentResponse>builder()
        .id(commentUpdated.getId().toString())
        .event(commentUpdated.getName())
        .data(commentUpdated.getComment())
        .build();
  }
}
