package com.github.devraghav.issue.repository;

import java.util.Map;
import lombok.Getter;

public class IssueNotFoundException extends RuntimeException {
  private static final String MESSAGE = "Issue not found";
  @Getter private final Map<String, Object> meta;

  public IssueNotFoundException(String issueId) {
    super(MESSAGE);
    this.meta = Map.of("id", issueId);
  }
}
