package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.IssueComment;
import com.github.devraghav.bugtracker.issue.dto.IssueCommentRequest;
import com.github.devraghav.bugtracker.issue.dto.IssueException;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import com.github.devraghav.bugtracker.issue.repository.IssueCommentRepository;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IssueCommentPersistService {

  private final UserService userService;
  private final IssueCommentFetchService issueCommentFetchService;
  private final IssueService issueService;
  private final IssueCommentRepository issueCommentRepository;

  public Mono<IssueComment> save(String issueId, IssueCommentRequest issueCommentRequest) {
    return validate(issueCommentRequest)
        .and(issueService.exists(issueId))
        .thenReturn(issueCommentRequest)
        .map(IssueCommentEntity::new)
        .flatMap(comment -> issueCommentRepository.save(issueId, comment))
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
        .flatMap(this::validateUserId)
        .thenReturn(issueCommentRequest);
  }

  private Mono<Boolean> validateUserId(String userId) {
    return userService
        .exists(userId)
        .onErrorResume(
            UserException.class, exception -> Mono.error(() -> IssueException.invalidUser(userId)));
  }

  private Mono<IssueCommentRequest> validateCommentContent(
      IssueCommentRequest issueCommentRequest) {
    return Mono.just(issueCommentRequest)
        .filter(IssueCommentRequest::isContentValid)
        .switchIfEmpty(
            Mono.error(() -> IssueException.invalidComment(issueCommentRequest.getContent())));
  }
}
