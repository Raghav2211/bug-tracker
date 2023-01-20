package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.request.CommentRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
record CreateCommentRequestValidator(IssueRepository issueRepository)
    implements Validator<CommentRequest.CreateComment, CommentRequest.CreateComment> {

  @Override
  public Mono<CommentRequest.CreateComment> validate(CommentRequest.CreateComment createComment) {
    return validateCommentContent(createComment.content())
        .and(validateIssueId(createComment.issueId()))
        .thenReturn(createComment);
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
