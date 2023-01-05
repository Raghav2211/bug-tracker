package com.github.devraghav.bugtracker.issue.dto;

import java.util.Map;
import java.util.Set;

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
}
