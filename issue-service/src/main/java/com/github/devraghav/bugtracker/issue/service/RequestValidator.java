package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.project.ProjectResponse;
import com.github.devraghav.bugtracker.issue.request.CommentRequest;
import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.validation.Validator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
@RequiredArgsConstructor
public class RequestValidator {

  private final Validator<
          IssueRequest.CreateIssue,
          Tuple2<
              IssueRequest.CreateIssue,
              List<Tuple2<ProjectResponse.Project, ProjectResponse.Project.Version>>>>
      createIssueRequestValidator;
  private final Validator<CommentRequest.CreateComment, CommentRequest.CreateComment>
      createCommentRequestValidator;
  private final Validator<CommentRequest.UpdateComment, CommentRequest.UpdateComment>
      updateCommentRequestValidator;

  public Mono<
          Tuple2<
              IssueRequest.CreateIssue,
              List<Tuple2<ProjectResponse.Project, ProjectResponse.Project.Version>>>>
      validate(final IssueRequest.CreateIssue request) {
    return createIssueRequestValidator.validate(request);
  }

  public Mono<CommentRequest.CreateComment> validate(final CommentRequest.CreateComment request) {
    return createCommentRequestValidator.validate(request);
  }

  public Mono<CommentRequest.UpdateComment> validate(final CommentRequest.UpdateComment request) {
    return updateCommentRequestValidator.validate(request);
  }
}
