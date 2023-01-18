package com.github.devraghav.bugtracker.issue.dto;

import java.util.Map;
import java.util.Set;

public interface IssueRequest {

  record Create(
      Priority priority,
      Severity severity,
      String businessUnit,
      Set<ProjectInfo> projects,
      String header,
      String description,
      Map<String, String> tags) {
    public Create {
      projects = Set.copyOf(projects == null ? Set.of() : projects);
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  record Update(
      Priority priority,
      Severity severity,
      String businessUnit,
      String header,
      String description,
      // TODO : updateBy feature
      String updateBy,
      Map<String, String> tags) {
    public Update {
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  record Assign(String issueId, String user, MonitorType monitorType, String requestedBy) {}

  record CreateComment(String userId, String issueId, String content) {}

  record UpdateComment(String userId, String issueId, String commentId, String content) {}
}
