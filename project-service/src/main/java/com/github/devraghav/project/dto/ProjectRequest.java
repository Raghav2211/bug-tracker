package com.github.devraghav.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import org.springframework.util.StringUtils;

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
  public boolean isDescriptionValid() {
    return StringUtils.hasLength(this.description) && this.description.length() <= 20;
  }

  @JsonIgnore
  public boolean isAuthorNotNull() {
    return StringUtils.hasLength(this.author);
  }

  @JsonIgnore
  public boolean isNameValid() {
    return StringUtils.hasLength(this.name) && this.name().matches("^[a-zA-Z]*$");
  }
}
