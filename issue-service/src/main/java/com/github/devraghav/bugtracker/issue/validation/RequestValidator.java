package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.IssueRequests;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(
    Validator<IssueRequests.Create, IssueRequests.Create> createIssueRequestValidator,
    Validator<IssueRequests.CreateComment, IssueRequests.CreateComment>
        createCommentRequestValidator,
    Validator<IssueRequests.UpdateComment, IssueRequests.UpdateComment>
        updateCommentRequestValidator) {

  public Mono<IssueRequests.Create> validate(final IssueRequests.Create request) {
    return createIssueRequestValidator.validate(request);
  }

  public Mono<IssueRequests.CreateComment> validate(final IssueRequests.CreateComment request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<IssueRequests.UpdateComment> validate(final IssueRequests.UpdateComment request) {
    return updateCommentRequestValidator.validate(request);
  }
}
