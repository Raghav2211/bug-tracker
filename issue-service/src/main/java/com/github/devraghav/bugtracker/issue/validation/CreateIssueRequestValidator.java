package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.exception.ProjectClientException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.project.ProjectClient;
import com.github.devraghav.bugtracker.issue.project.ProjectResponse;
import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
@RequiredArgsConstructor
class CreateIssueRequestValidator
    implements Validator<
        IssueRequest.CreateIssue,
        Tuple2<
            IssueRequest.CreateIssue,
            List<Tuple2<ProjectResponse.Project, ProjectResponse.Project.Version>>>> {
  private final ProjectClient projectClient;

  @Override
  public Mono<
          Tuple2<
              IssueRequest.CreateIssue,
              List<Tuple2<ProjectResponse.Project, ProjectResponse.Project.Version>>>>
      validate(IssueRequest.CreateIssue createIssue) {
    var validRequest =
        validateHeader(createIssue.header())
            .and(validateDescription(createIssue.description()))
            .and(validatePriority(createIssue.priority()))
            .and(validateSeverity(createIssue.severity()))
            .thenReturn(createIssue);
    var projectAttachments = validatedProjectAttachments(createIssue.attachments());
    return validRequest.zipWith(projectAttachments);
  }

  private Mono<Void> validateHeader(String header) {
    return Mono.justOrEmpty(header)
        .filter(issueHeader -> StringUtils.hasText(issueHeader) && issueHeader.length() <= 200)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidSummary(header)))
        .then();
  }

  private Mono<Void> validateDescription(String description) {
    return Mono.justOrEmpty(description)
        .filter(
            issueDescription ->
                StringUtils.hasText(issueDescription) && issueDescription.length() <= 3000)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidDescription(description)))
        .then();
  }

  private Mono<Void> validatePriority(IssueResponse.Priority priority) {
    return Mono.justOrEmpty(priority)
        .filter(Objects::nonNull)
        .switchIfEmpty(Mono.error(IssueException::nullPriority))
        .then();
  }

  private Mono<Void> validateSeverity(IssueResponse.Severity severity) {
    return Mono.justOrEmpty(severity)
        .filter(Objects::nonNull)
        .switchIfEmpty(Mono.error(IssueException::nullSeverity))
        .then();
  }

  private Mono<List<Tuple2<ProjectResponse.Project, ProjectResponse.Project.Version>>>
      validatedProjectAttachments(Collection<IssueRequest.ProjectAttachment> projectAttachments) {
    return Flux.fromIterable(projectAttachments)
        .switchIfEmpty(Mono.error(IssueException::noProjectAttach))
        .flatMap(this::validateProjectAttachments)
        .collectList();
  }

  private Mono<Tuple2<ProjectResponse.Project, ProjectResponse.Project.Version>>
      validateProjectAttachments(IssueRequest.ProjectAttachment projectAttachment) {
    return Mono.zip(
        getProject(projectAttachment.projectId()),
        getProjectVersion(projectAttachment.projectId(), projectAttachment.versionId()));
  }

  private Mono<ProjectResponse.Project> getProject(String projectId) {
    return projectClient
        .getProjectById(projectId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }

  private Mono<ProjectResponse.Project.Version> getProjectVersion(
      String projectId, String versionId) {
    return projectClient
        .getVersionById(projectId, versionId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }
}
