package com.github.devraghav.bugtracker.project.dto;

import java.util.Map;

public record CreateProjectRequest(
    String name,
    String description,
    ProjectStatus status,
    String author,
    Map<String, Object> tags) {
  public CreateProjectRequest {
    tags = Map.copyOf(tags == null ? Map.of() : tags);
  }
}
