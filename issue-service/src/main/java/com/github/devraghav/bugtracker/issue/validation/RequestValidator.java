package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.CreateCommentRequest;
import com.github.devraghav.bugtracker.issue.dto.CreateIssueRequest;
import com.github.devraghav.bugtracker.issue.dto.UpdateCommentRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(
    Validator<CreateIssueRequest, CreateIssueRequest> createIssueRequestValidator,
    Validator<CreateCommentRequest, CreateCommentRequest> createCommentRequestValidator,
    Validator<UpdateCommentRequest, UpdateCommentRequest> updateCommentRequestValidator) {

  public Mono<CreateIssueRequest> validate(final CreateIssueRequest request) {
    return createIssueRequestValidator.validate(request);
  }

  public Mono<CreateCommentRequest> validate(final CreateCommentRequest request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<UpdateCommentRequest> validate(final UpdateCommentRequest request) {
    return updateCommentRequestValidator.validate(request);
  }
}
