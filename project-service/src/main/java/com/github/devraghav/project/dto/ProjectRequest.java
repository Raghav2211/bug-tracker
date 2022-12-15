package com.github.devraghav.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public record ProjectRequest(
    String name,
    String description,
    ProjectStatus status,
    String author,
    Map<String, Object> tags) {
  public ProjectRequest {
    tags = Map.copyOf(tags == null ? Map.of() : tags);
  }

  @JsonIgnore
  private boolean isDescriptionValid() {
    return StringUtils.hasLength(this.description) && this.description.length() <= 20;
  }

  @JsonIgnore
  private boolean isAuthorNotNull() {
    return StringUtils.hasLength(this.author);
  }

  @JsonIgnore
  private boolean isNameValid() {
    return StringUtils.hasLength(this.name) && this.name().matches("^[a-zA-Z]*$");
  }

  private Mono<ProjectRequest> validateName(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .filter(ProjectRequest::isNameValid)
        .switchIfEmpty(Mono.error(() -> ProjectException.invalidName(projectRequest.name())));
  }

  private Mono<ProjectRequest> validateDescription(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .filter(ProjectRequest::isDescriptionValid)
        .switchIfEmpty(
            Mono.error(() -> ProjectException.invalidDescription(projectRequest.description())));
  }

  private Mono<ProjectRequest> validateAuthor(ProjectRequest projectRequest) {
    return Mono.just(projectRequest)
        .filter(ProjectRequest::isAuthorNotNull)
        .switchIfEmpty(Mono.error(ProjectException::nullAuthor))
        .thenReturn(projectRequest);
  }

  public Mono<ProjectRequest> validate() {
    return Mono.just(this)
        .and(validateName(this))
        .and(validateDescription(this))
        .and(validateAuthor(this))
        .thenReturn(this);
  }
}
