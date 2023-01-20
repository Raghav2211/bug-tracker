package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.exception.ProjectClientException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import com.github.devraghav.bugtracker.issue.service.ProjectReactiveClient;
import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class CreateIssueRequestValidator
    implements Validator<IssueRequest.CreateIssue, IssueRequest.CreateIssue> {

  private final ProjectReactiveClient projectReactiveClient;

  @Override
  public Mono<IssueRequest.CreateIssue> validate(IssueRequest.CreateIssue createIssue) {
    return validateHeader(createIssue.header())
        .and(validateDescription(createIssue.description()))
        .and(validatePriority(createIssue.priority()))
        .and(validateSeverity(createIssue.severity()))
        .and(validatedProjectInfo(createIssue.projects()))
        .thenReturn(createIssue);
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

  private Mono<Void> validatedProjectInfo(Collection<IssueRequest.ProjectInfo> projectInfos) {
    return Flux.fromIterable(projectInfos)
        .switchIfEmpty(Mono.error(IssueException::noProjectAttach))
        .flatMap(this::validateProjectInfo)
        .last()
        .then();
  }

  private Mono<Boolean> validateProjectInfo(IssueRequest.ProjectInfo projectInfo) {
    return Mono.just(projectInfo)
        .filter(IssueRequest.ProjectInfo::isValid)
        .flatMap(this::isProjectInfoExists)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidProject(projectInfo)));
  }

  private Mono<Boolean> isProjectInfoExists(IssueRequest.ProjectInfo projectInfo) {
    return Mono.zip(
            validateProjectId(projectInfo.projectId()),
            validateProjectVersion(projectInfo.projectId(), projectInfo.versionId()),
            Boolean::logicalAnd)
        .map(Boolean::booleanValue);
  }

  private Mono<Boolean> validateProjectId(String projectId) {
    return projectReactiveClient
        .isProjectExists(projectId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }

  private Mono<Boolean> validateProjectVersion(String projectId, String versionId) {
    return projectReactiveClient
        .isProjectVersionExists(projectId, versionId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }
}
