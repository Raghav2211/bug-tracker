package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.IssueComment;
import com.github.devraghav.bugtracker.issue.dto.IssueCommentRequest;
import com.github.devraghav.bugtracker.issue.dto.IssueException;
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
    IssueCommentRepository issueCommentRepository) {

  public Mono<IssueComment> save(String issueId, IssueCommentRequest issueCommentRequest) {
    return validate(issueCommentRequest)
        .and(issueService.exists(issueId))
        .thenReturn(issueCommentRequest)
        .map(
            _issueCommentRequest ->
                issueCommentMapper.requestToEntity(issueId, _issueCommentRequest))
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
    return Mono.just(issueCommentRequest.userId())
        .flatMap(userReactiveClient::fetchUser)
        .thenReturn(issueCommentRequest);
  }

  private Mono<IssueCommentRequest> validateCommentContent(
      IssueCommentRequest issueCommentRequest) {
    return Mono.just(issueCommentRequest)
        .filter(IssueCommentRequest::isContentValid)
        .switchIfEmpty(
            Mono.error(() -> IssueException.invalidComment(issueCommentRequest.content())));
  }
}
