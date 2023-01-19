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
record CreateCommentRequestValidator(
    IssueRepository issueRepository, UserReactiveClient userReactiveClient)
    implements Validator<IssueRequest.CreateComment, IssueRequest.CreateComment> {

  @Override
  public Mono<IssueRequest.CreateComment> validate(
      IssueRequest.CreateComment createCommentRequest) {
    return validateCommentContent(createCommentRequest.content())
        .and(
            Mono.zip(
                validateCommentUserId(createCommentRequest.userId()),
                validateIssueId(createCommentRequest.issueId())))
        .thenReturn(createCommentRequest);
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
