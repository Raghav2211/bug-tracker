package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.request.CommentRequest;
import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {

  private final Validator<IssueRequest.CreateIssue, IssueRequest.CreateIssue>
      createIssueRequestValidator;
  private final Validator<CommentRequest.CreateComment, CommentRequest.CreateComment>
      createCommentRequestValidator;
  private final Validator<CommentRequest.UpdateComment, CommentRequest.UpdateComment>
      updateCommentRequestValidator;

  public Mono<IssueRequest.CreateIssue> validate(final IssueRequest.CreateIssue request) {
    return createIssueRequestValidator.validate(request);
  }

  public Mono<CommentRequest.CreateComment> validate(final CommentRequest.CreateComment request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<CommentRequest.UpdateComment> validate(final CommentRequest.UpdateComment request) {
    return updateCommentRequestValidator.validate(request);
  }
}
