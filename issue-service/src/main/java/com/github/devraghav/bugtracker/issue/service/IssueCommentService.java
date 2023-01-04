package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import com.github.devraghav.bugtracker.issue.kafka.producer.KafkaProducer;
import com.github.devraghav.bugtracker.issue.mapper.IssueCommentMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueCommentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record IssueCommentService(
    UserReactiveClient userReactiveClient,
    IssueCommentMapper issueCommentMapper,
    IssueService issueService,
    IssueCommentFetchService issueCommentFetchService,
    IssueCommentRepository issueCommentRepository,
    KafkaProducer kafkaProducer) {

  public Mono<IssueComment> save(
      String requestId, String issueId, CreateCommentRequest createCommentRequest) {
    return validate(issueId, createCommentRequest)
        .thenReturn(createCommentRequest)
        .map(
            validCommentRequest -> issueCommentMapper.requestToEntity(issueId, validCommentRequest))
        .flatMap(issueCommentRepository::save)
        .flatMap(issueCommentFetchService::getComment)
        .flatMap(
            issueComment -> kafkaProducer.sendCommentAddedEvent(requestId, issueId, issueComment));
  }

  public Mono<IssueComment> update(
      String requestId,
      String issueId,
      String commentId,
      UpdateCommentRequest updateCommentRequest) {
    return validateAndUpdateIssueCommentEntity(issueId, commentId, updateCommentRequest)
        .flatMap(issueCommentRepository::save)
        .flatMap(issueCommentFetchService::getComment)
        .flatMap(
            issueComment ->
                kafkaProducer.sendCommentUpdatedEvent(requestId, issueId, issueComment));
  }

  private Mono<IssueCommentEntity> validateAndUpdateIssueCommentEntity(
      String issueId, String commentId, UpdateCommentRequest updateCommentRequest) {

    var issueExistsMono = issueService.exists(issueId);
    var commentMono =
        issueCommentRepository
            .findById(commentId)
            .switchIfEmpty(Mono.error(() -> IssueException.invalidComment(commentId)));
    return validate(updateCommentRequest)
        .zipWith(
            Mono.zip(issueExistsMono, commentMono, (issueExists, comment) -> comment),
            this::updateIssueCommentEntity);
  }

  private Mono<CreateCommentRequest> validate(
      String issueId, CreateCommentRequest createCommentRequest) {
    return validateCommentContent(createCommentRequest)
        .and(
            Mono.zip(
                validateCommentUserId(createCommentRequest.userId()), issueService.exists(issueId)))
        .thenReturn(createCommentRequest);
  }

  private Mono<UpdateCommentRequest> validate(UpdateCommentRequest updateCommentRequest) {
    return validateCommentContent(updateCommentRequest).thenReturn(updateCommentRequest);
  }

  private Mono<User> validateCommentUserId(String userId) {
    return userReactiveClient
        .fetchUser(userId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(IssueException.userServiceException(exception)));
  }

  private Mono<CommentRequest> validateCommentContent(CommentRequest commentRequest) {
    return Mono.just(commentRequest)
        .filter(CommentRequest::isContentValid)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidComment(commentRequest.content())));
  }

  private IssueCommentEntity updateIssueCommentEntity(
      UpdateCommentRequest updateCommentRequest, IssueCommentEntity issueCommentEntity) {
    issueCommentEntity.setContent(updateCommentRequest.content());
    return issueCommentEntity;
  }
}
