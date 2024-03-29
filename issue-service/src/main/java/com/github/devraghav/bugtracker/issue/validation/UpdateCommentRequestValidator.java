package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.request.CommentRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
record UpdateCommentRequestValidator(IssueRepository issueRepository)
    implements Validator<CommentRequest.UpdateComment, CommentRequest.UpdateComment> {

  @Override
  public Mono<CommentRequest.UpdateComment> validate(CommentRequest.UpdateComment updateComment) {
    return validateCommentContent(updateComment.content())
        .and(validateIssueId(updateComment.issueId()))
        .thenReturn(updateComment);
  }

  private Mono<Void> validateCommentContent(String content) {
    return Mono.just(content)
        .filter(
            commentContent ->
                StringUtils.hasLength(commentContent) && commentContent.length() <= 256)
        .switchIfEmpty(Mono.error(() -> CommentException.invalidComment(content)))
        .then();
  }

  public Mono<Boolean> validateIssueId(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }
}
