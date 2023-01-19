package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.IssueRequest;
import com.github.devraghav.bugtracker.issue.dto.User;
import com.github.devraghav.bugtracker.issue.exception.UserClientException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {

  private final Validator<IssueRequest.Create, IssueRequest.Create> createIssueRequestValidator;
  private final Validator<IssueRequest.CreateComment, IssueRequest.CreateComment>
      createCommentRequestValidator;
  private final Validator<IssueRequest.UpdateComment, IssueRequest.UpdateComment>
      updateCommentRequestValidator;
  private final UserReactiveClient userReactiveClient;

  public Mono<IssueRequest.Create> validate(
      final String reporter, final IssueRequest.Create request) {
    return Mono.zip(createIssueRequestValidator.validate(request), validateReporter(reporter))
        .thenReturn(request);
  }

  public Mono<String> validateReporter(String reporter) {
    return fetchUser(reporter).thenReturn(reporter);
  }

  private Mono<User> fetchUser(String userId) {
    return userReactiveClient
        .fetchUser(userId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(IssueException.userServiceException(exception)));
  }

  public Mono<IssueRequest.CreateComment> validate(final IssueRequest.CreateComment request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<IssueRequest.UpdateComment> validate(final IssueRequest.UpdateComment request) {
    return updateCommentRequestValidator.validate(request);
  }
}
