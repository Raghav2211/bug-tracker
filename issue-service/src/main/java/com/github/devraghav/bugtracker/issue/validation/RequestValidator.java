package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.IssueRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(
    Validator<IssueRequest.Create, IssueRequest.Create> createIssueRequestValidator,
    Validator<IssueRequest.CreateComment, IssueRequest.CreateComment> createCommentRequestValidator,
    Validator<IssueRequest.UpdateComment, IssueRequest.UpdateComment>
        updateCommentRequestValidator) {

  public Mono<IssueRequest.Create> validate(final IssueRequest.Create request) {
    return createIssueRequestValidator.validate(request);
  }

  public Mono<IssueRequest.CreateComment> validate(final IssueRequest.CreateComment request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<IssueRequest.UpdateComment> validate(final IssueRequest.UpdateComment request) {
    return updateCommentRequestValidator.validate(request);
  }
}
