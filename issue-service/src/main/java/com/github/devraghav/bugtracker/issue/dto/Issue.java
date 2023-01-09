package com.github.devraghav.bugtracker.issue.dto;

import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

@Getter
@Builder
@ToString
public class Issue {
  private String id;
  private Priority priority;
  private Severity severity;
  private String businessUnit;
  private List<Project> projects;
  private String header;
  private String description;
  private User assignee;
  private User reporter;
  private Set<User> watchers;
  private Map<String, String> tags;
  private LocalDateTime createdAt;
  private LocalDateTime endedAt;

  public static class IssueBuilder {
    public IssueBuilder() {}
  }
}
