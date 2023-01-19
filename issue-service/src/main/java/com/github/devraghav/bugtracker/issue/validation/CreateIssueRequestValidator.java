package com.github.devraghav.bugtracker.issue.validation;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.exception.ProjectClientException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.service.ProjectReactiveClient;
import com.github.devraghav.bugtracker.issue.service.UserReactiveClient;
import java.util.Collection;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// TODO: check user have write access
@Component
@RequiredArgsConstructor
class CreateIssueRequestValidator implements Validator<IssueRequest.Create, IssueRequest.Create> {

  private final ProjectReactiveClient projectReactiveClient;
  private final UserReactiveClient userReactiveClient;

  @Override
  public Mono<IssueRequest.Create> validate(IssueRequest.Create createIssueRequest) {
    return validateHeader(createIssueRequest.header())
        .and(validateDescription(createIssueRequest.description()))
        .and(validatePriority(createIssueRequest.priority()))
        .and(validateSeverity(createIssueRequest.severity()))
        .and(validatedProjectInfo(createIssueRequest.projects()))
        .thenReturn(createIssueRequest);
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

  private Mono<Void> validatePriority(Priority priority) {
    return Mono.justOrEmpty(priority)
        .filter(Objects::nonNull)
        .switchIfEmpty(Mono.error(IssueException::nullPriority))
        .then();
  }

  private Mono<Void> validateSeverity(Severity severity) {
    return Mono.justOrEmpty(severity)
        .filter(Objects::nonNull)
        .switchIfEmpty(Mono.error(IssueException::nullSeverity))
        .then();
  }

  private Mono<Void> validatedProjectInfo(Collection<ProjectInfo> projectInfos) {
    return Flux.fromIterable(projectInfos)
        .switchIfEmpty(Mono.error(IssueException::noProjectAttach))
        .flatMap(this::validateProjectInfo)
        .last()
        .then();
  }

  private Mono<Boolean> validateProjectInfo(ProjectInfo projectInfo) {
    return Mono.just(projectInfo)
        .filter(ProjectInfo::isValid)
        .flatMap(this::isProjectInfoExists)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidProject(projectInfo)));
  }

  private Mono<Boolean> isProjectInfoExists(ProjectInfo projectInfo) {
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
