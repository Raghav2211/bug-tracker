package com.github.devraghav.bugtracker.issue.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Data;

@Data
public class IssueUpdateRequest {
  private Priority priority;
  private Severity severity;
  private String businessUnit;
  private String header;
  private String description;
  private Map<String, String> tags = new HashMap<>();

  public Optional<Priority> getPriority() {
    return Optional.ofNullable(priority);
  }

  public Optional<Severity> getSeverity() {
    return Optional.ofNullable(severity);
  }

  public Optional<String> getBusinessUnit() {
    return Optional.ofNullable(businessUnit);
  }

  public Optional<String> getHeader() {
    return Optional.ofNullable(header);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<Map<String, String>> getTags() {
    return Optional.ofNullable(tags);
  }
}
