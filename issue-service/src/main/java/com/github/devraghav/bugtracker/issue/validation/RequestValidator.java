package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.CreateIssueRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(
    ValidationStrategy<CreateIssueRequest> createIssueRequestValidationStrategy) {

  public Mono<CreateIssueRequest> validate(final CreateIssueRequest request) {
    return createIssueRequestValidationStrategy.validate(request);
  }
}
