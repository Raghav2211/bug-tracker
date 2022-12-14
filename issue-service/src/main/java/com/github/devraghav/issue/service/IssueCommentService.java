package com.github.devraghav.issue.service;

import com.github.devraghav.issue.dto.IssueComment;
import com.github.devraghav.issue.dto.IssueCommentRequest;
import com.github.devraghav.issue.dto.IssueException;
import com.github.devraghav.issue.entity.IssueCommentEntity;
import com.github.devraghav.issue.repository.IssueCommentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public record IssueCommentService(
    IssueCommentFetchService issueCommentFetchService,
    IssueService issueService,
    IssueCommentRepository issueCommentRepository) {

  public Mono<IssueComment> save(String issueId, IssueCommentRequest issueCommentRequest) {
    return validate(issueCommentRequest)
        .and(issueService.exists(issueId))
        .thenReturn(issueCommentRequest)
        .map(_issueCommentRequest -> new IssueCommentEntity(_issueCommentRequest, issueId))
        .flatMap(issueCommentRepository::save)
        .flatMap(issueCommentFetchService::getComment);
  }

  private Mono<IssueCommentRequest> validate(IssueCommentRequest issueCommentRequest) {
    return Mono.just(issueCommentRequest)
        .and(validateCommentUserId(issueCommentRequest))
        .and(validateCommentContent(issueCommentRequest))
        .thenReturn(issueCommentRequest);
  }

  private Mono<IssueCommentRequest> validateCommentUserId(IssueCommentRequest issueCommentRequest) {
    return Mono.just(issueCommentRequest.getUserId())
        .flatMap(issueService::getUser)
        .thenReturn(issueCommentRequest);
  }

  private Mono<IssueCommentRequest> validateCommentContent(
      IssueCommentRequest issueCommentRequest) {
    return Mono.just(issueCommentRequest)
        .filter(IssueCommentRequest::isContentValid)
        .switchIfEmpty(
            Mono.error(() -> IssueException.invalidComment(issueCommentRequest.getContent())));
  }
}
