package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.exception.UserClientException;
import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.service.UserReactiveClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
record UpdateCommentRequestValidator(
    IssueRepository issueRepository, UserReactiveClient userReactiveClient)
    implements Validator<IssueRequest.UpdateComment, IssueRequest.UpdateComment> {

  @Override
  public Mono<IssueRequest.UpdateComment> validate(
      IssueRequest.UpdateComment updateCommentRequest) {
    return validateCommentContent(updateCommentRequest.content())
        .and(
            Mono.zip(
                validateCommentUserId(updateCommentRequest.userId()),
                validateIssueId(updateCommentRequest.issueId())))
        .thenReturn(updateCommentRequest);
  }

  private Mono<Void> validateCommentContent(String content) {
    return Mono.just(content)
        .filter(
            commentContent ->
                StringUtils.hasLength(commentContent) && commentContent.length() <= 256)
        .switchIfEmpty(Mono.error(() -> CommentException.invalidComment(content)))
        .then();
  }

  private Mono<User> validateCommentUserId(String userId) {
    return userReactiveClient
        .fetchUser(userId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(CommentException.userServiceException(exception)));
  }

  public Mono<Boolean> validateIssueId(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }
}
