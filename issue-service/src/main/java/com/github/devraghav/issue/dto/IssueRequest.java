package com.github.devraghav.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public record IssueRequest(
    Priority priority,
    Severity severity,
    String businessUnit,
    Set<ProjectInfo> projects,
    String header,
    String description,
    String reporter,
    Map<String, String> tags) {
  public IssueRequest {
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

  private Mono<IssueRequest> validateHeader(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::isHeaderValid)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidSummary(issueRequest.header())));
  }

  private Mono<IssueRequest> validateDescription(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::isDescriptionValid)
        .switchIfEmpty(
            Mono.error(() -> IssueException.invalidDescription(issueRequest.description())));
  }

  private Mono<IssueRequest> validatePriority(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::hasPriority)
        .switchIfEmpty(Mono.error(IssueException::nullPriority))
        .thenReturn(issueRequest);
  }

  private Mono<IssueRequest> validateSeverity(IssueRequest issueRequest) {
    return Mono.just(issueRequest)
        .filter(IssueRequest::hasSeverity)
        .switchIfEmpty(Mono.error(IssueException::nullSeverity))
        .thenReturn(issueRequest);
  }

  public Mono<IssueRequest> validate() {
    return Mono.just(this)
        .and(validateHeader(this))
        .and(validateDescription(this))
        .and(validatePriority(this))
        .and(validateSeverity(this))
        .thenReturn(this);
  }
}
