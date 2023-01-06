package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.CreateCommentRequest;
import com.github.devraghav.bugtracker.issue.dto.CreateIssueRequest;
import com.github.devraghav.bugtracker.issue.dto.UpdateCommentRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(
    ValidationStrategy<CreateIssueRequest> createIssueRequestValidationStrategy,
    CreateCommentRequestValidationStrategy createCommentRequestValidationStrategy,
    UpdateCommentRequestValidationStrategy updateCommentRequestValidationStrategy) {

  public Mono<CreateIssueRequest> validate(final CreateIssueRequest request) {
    return createIssueRequestValidationStrategy.validate(request);
  }

  public Mono<CreateCommentRequest> validate(final CreateCommentRequest request) {
    return createCommentRequestValidationStrategy.validate(request);
  }

  public Mono<UpdateCommentRequest> validate(final UpdateCommentRequest request) {
    return updateCommentRequestValidationStrategy.validate(request);
  }
}
