package com.github.devraghav.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Set;
import org.springframework.util.StringUtils;

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
  public boolean hasSeverity() {
    return this.severity != null;
  }

  @JsonIgnore
  public boolean hasPriority() {
    return this.priority != null;
  }

  @JsonIgnore
  public boolean isHeaderValid() {
    return StringUtils.hasText(this.header) && this.header.length() <= 50;
  }

  @JsonIgnore
  public boolean isDescriptionValid() {
    return StringUtils.hasText(this.description) && this.description.length() <= 200;
  }
}
