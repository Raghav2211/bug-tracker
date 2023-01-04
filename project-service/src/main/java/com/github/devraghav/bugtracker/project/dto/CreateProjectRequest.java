package com.github.devraghav.bugtracker.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public record CreateProjectRequest(
    String name,
    String description,
    ProjectStatus status,
    String author,
    Map<String, Object> tags) {
  public CreateProjectRequest {
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

  private Mono<CreateProjectRequest> validateName(
      Mono<CreateProjectRequest> createProjectRequestMono) {
    return createProjectRequestMono
        .filter(CreateProjectRequest::isNameValid)
        .switchIfEmpty(Mono.error(() -> ProjectException.invalidName(this.name())));
  }

  private Mono<CreateProjectRequest> validateDescription(
      Mono<CreateProjectRequest> createProjectRequestMono) {
    return createProjectRequestMono
        .filter(CreateProjectRequest::isDescriptionValid)
        .switchIfEmpty(Mono.error(() -> ProjectException.invalidDescription(this.description())));
  }

  private Mono<CreateProjectRequest> validateAuthor(
      Mono<CreateProjectRequest> createProjectRequestMono) {
    return createProjectRequestMono
        .filter(CreateProjectRequest::isAuthorNotNull)
        .switchIfEmpty(Mono.error(ProjectException::nullAuthor));
  }

  public Mono<CreateProjectRequest> validate() {
    var createProjectRequestMono = Mono.just(this);
    return createProjectRequestMono
        .and(validateName(createProjectRequestMono))
        .and(validateDescription(createProjectRequestMono))
        .and(validateAuthor(createProjectRequestMono))
        .thenReturn(this);
  }
}
