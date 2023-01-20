package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.CommentRequestResponse;
import com.github.devraghav.bugtracker.issue.dto.IssueRequestResponse;
import com.github.devraghav.bugtracker.issue.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {

  private final Validator<
          IssueRequestResponse.CreateIssueRequest, IssueRequestResponse.CreateIssueRequest>
      createIssueRequestValidator;
  private final Validator<
          CommentRequestResponse.CreateCommentRequest, CommentRequestResponse.CreateCommentRequest>
      createCommentRequestValidator;
  private final Validator<
          CommentRequestResponse.UpdateCommentRequest, CommentRequestResponse.UpdateCommentRequest>
      updateCommentRequestValidator;

  public Mono<IssueRequestResponse.CreateIssueRequest> validate(
      final IssueRequestResponse.CreateIssueRequest request) {
    return createIssueRequestValidator.validate(request);
  }

  public Mono<CommentRequestResponse.CreateCommentRequest> validate(
      final CommentRequestResponse.CreateCommentRequest request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<CommentRequestResponse.UpdateCommentRequest> validate(
      final CommentRequestResponse.UpdateCommentRequest request) {
    return updateCommentRequestValidator.validate(request);
  }
}
