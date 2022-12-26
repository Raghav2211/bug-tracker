package com.github.devraghav.bugtracker.issue.dto;

import java.util.Map;

public record IssueUpdateRequest(
    Priority priority,
    Severity severity,
    String businessUnit,
    String header,
    String description,
    // TODO : updateBy feature
    String updateBy,
    Map<String, String> tags) {
  public IssueUpdateRequest {
    tags = Map.copyOf(tags == null ? Map.of() : tags);
  }
}
