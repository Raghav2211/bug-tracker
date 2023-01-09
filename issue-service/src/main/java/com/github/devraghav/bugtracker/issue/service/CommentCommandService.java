package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.bugtracker.issue.mapper.CommentMapper;
import com.github.devraghav.bugtracker.issue.pubsub.ReactiveMessageBroker;
import com.github.devraghav.bugtracker.issue.pubsub.ReactivePublisher;
import com.github.devraghav.bugtracker.issue.repository.CommentRepository;
import com.github.devraghav.bugtracker.issue.validation.RequestValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class CommentCommandService {

  private final RequestValidator requestValidator;
  private final CommentMapper commentMapper;
  private final CommentRepository commentRepository;
  private final UserReactiveClient userReactiveClient;
  private final ReactivePublisher<DomainEvent> eventReactivePublisher;
  private final ReactivePublisher<Comment> commentStreamPublisher;

  public CommentCommandService(
      RequestValidator requestValidator,
      CommentMapper commentMapper,
      CommentRepository commentRepository,
      UserReactiveClient userReactiveClient,
      ReactivePublisher<DomainEvent> eventReactivePublisher,
      ReactiveMessageBroker<Comment> reactiveMessageBroker) {
    this.requestValidator = requestValidator;
    this.commentMapper = commentMapper;
    this.commentRepository = commentRepository;
    this.userReactiveClient = userReactiveClient;
    this.eventReactivePublisher = eventReactivePublisher;
    this.commentStreamPublisher = new CommentStreamPublisher(reactiveMessageBroker);
  }

  public Mono<Comment> save(CreateCommentRequest createCommentRequest) {
    return requestValidator
        .validate(createCommentRequest)
        .thenReturn(createCommentRequest)
        .map(commentMapper::requestToEntity)
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .flatMap(this::processSaveComment);
  }

  public Mono<Comment> update(UpdateCommentRequest updateCommentRequest) {
    return requestValidator
        .validate(updateCommentRequest)
        .flatMap(
            validRequest ->
                findAndUpdateCommentContentById(validRequest.commentId(), validRequest.content()))
        .flatMap(commentRepository::save)
        .flatMap(this::getComment)
        .flatMap(this::processUpdateComment);
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

  private Mono<Comment> processSaveComment(Comment comment) {
    return eventReactivePublisher
        .publish(new CommentAddedEvent(comment.getIssueId(), comment))
        .then(commentStreamPublisher.publish(comment))
        .thenReturn(comment);
  }

  private Mono<Comment> processUpdateComment(Comment comment) {
    return eventReactivePublisher
        .publish(new CommentUpdatedEvent(comment.getIssueId(), comment))
        .then(commentStreamPublisher.publish(comment))
        .thenReturn(comment);
  }

  private class CommentStreamPublisher implements ReactivePublisher<Comment> {
    private final Sinks.Many<Comment> channel;

    private CommentStreamPublisher(ReactiveMessageBroker<Comment> commentReactiveMessageBroker) {
      this.channel = commentReactiveMessageBroker.getWriteChannel();
    }

    @Override
    public Mono<Void> publish(Comment message) {
      return Mono.fromRunnable(() -> channel.tryEmitNext(message));
    }
  }
}
