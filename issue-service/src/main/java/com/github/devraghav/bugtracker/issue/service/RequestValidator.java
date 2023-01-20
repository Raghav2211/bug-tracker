package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.RequestResponse;
import com.github.devraghav.bugtracker.issue.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {

  private final Validator<RequestResponse.CreateIssueRequest, RequestResponse.CreateIssueRequest>
      createIssueRequestValidator;
  private final Validator<
          RequestResponse.CreateCommentRequest, RequestResponse.CreateCommentRequest>
      createCommentRequestValidator;
  private final Validator<
          RequestResponse.UpdateCommentRequest, RequestResponse.UpdateCommentRequest>
      updateCommentRequestValidator;

  public Mono<RequestResponse.CreateIssueRequest> validate(
      final RequestResponse.CreateIssueRequest request) {
    return createIssueRequestValidator.validate(request);
  }

  public Mono<RequestResponse.CreateCommentRequest> validate(
      final RequestResponse.CreateCommentRequest request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<RequestResponse.UpdateCommentRequest> validate(
      final RequestResponse.UpdateCommentRequest request) {
    return updateCommentRequestValidator.validate(request);
  }
}
