package com.github.devraghav.bugtracker.issue.dto;

import java.util.Map;

public record UpdateIssueRequest(
    Priority priority,
    Severity severity,
    String businessUnit,
    String header,
    String description,
    // TODO : updateBy feature
    String updateBy,
    Map<String, String> tags) {
  public UpdateIssueRequest {
    tags = Map.copyOf(tags == null ? Map.of() : tags);
  }
}
