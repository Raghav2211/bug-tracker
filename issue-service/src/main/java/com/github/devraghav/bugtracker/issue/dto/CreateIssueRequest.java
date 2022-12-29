package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public record CreateIssueRequest(
    Priority priority,
    Severity severity,
    String businessUnit,
    Set<ProjectInfo> projects,
    String header,
    String description,
    String reporter,
    Map<String, String> tags) {
  public CreateIssueRequest {
    projects = Set.copyOf(projects == null ? Set.of() : projects);
    tags = Map.copyOf(tags == null ? Map.of() : tags);
  }

  @JsonIgnore
  private boolean hasSeverity() {
    return this.severity != null;
  }

  @JsonIgnore
  private boolean hasPriority() {
    return this.priority != null;
  }

  @JsonIgnore
  private boolean isHeaderValid() {
    return StringUtils.hasText(this.header) && this.header.length() <= 50;
  }

  @JsonIgnore
  private boolean isDescriptionValid() {
    return StringUtils.hasText(this.description) && this.description.length() <= 200;
  }

  private Mono<CreateIssueRequest> validateHeader() {
    return createIssueRequest()
        .filter(CreateIssueRequest::isHeaderValid)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidSummary(this.header())));
  }

  private Mono<CreateIssueRequest> validateDescription() {
    return createIssueRequest()
        .filter(CreateIssueRequest::isDescriptionValid)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidDescription(this.description())));
  }

  private Mono<CreateIssueRequest> validatePriority() {
    return createIssueRequest()
        .filter(CreateIssueRequest::hasPriority)
        .switchIfEmpty(Mono.error(IssueException::nullPriority));
  }

  private Mono<CreateIssueRequest> validateSeverity() {
    return createIssueRequest()
        .filter(CreateIssueRequest::hasSeverity)
        .switchIfEmpty(Mono.error(IssueException::nullSeverity));
  }

  private Mono<CreateIssueRequest> createIssueRequest() {
    return Mono.just(this);
  }

  public Mono<CreateIssueRequest> validate() {
    return createIssueRequest()
        .and(validateHeader())
        .and(validateDescription())
        .and(validatePriority())
        .and(validateSeverity())
        .thenReturn(this);
  }
}
