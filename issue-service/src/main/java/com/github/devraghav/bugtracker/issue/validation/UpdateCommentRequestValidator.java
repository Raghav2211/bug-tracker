package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.service.UserReactiveClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public record UpdateCommentRequestValidator(
    IssueRepository issueRepository, UserReactiveClient userReactiveClient)
    implements Validator<IssueRequests.UpdateComment, IssueRequests.UpdateComment> {

  @Override
  public Mono<IssueRequests.UpdateComment> validate(
      IssueRequests.UpdateComment updateCommentRequest) {
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
        .switchIfEmpty(Mono.error(() -> IssueException.invalidComment(content)))
        .then();
  }

  private Mono<User> validateCommentUserId(String userId) {
    return userReactiveClient
        .fetchUser(userId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(IssueException.userServiceException(exception)));
  }

  public Mono<Boolean> validateIssueId(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }
}
