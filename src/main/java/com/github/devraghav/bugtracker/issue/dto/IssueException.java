package com.github.devraghav.bugtracker.issue.dto;

import java.util.Map;
import lombok.Getter;

public class IssueException extends RuntimeException {
  @Getter private final Map<String, Object> meta;

  private IssueException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  private IssueException(String message) {
    this(message, Map.of());
  }

  public static IssueException invalidComment(String content) {
    return new IssueException(
        "Comment should not be null & less than 256 character length", Map.of("comment", content));
  }

  public static IssueException nullSeverity() {
    return new IssueException("Severity not found");
  }

  public static IssueException nullPriority() {
    return new IssueException("Priority not found");
  }

  public static IssueException invalidUser(String requestedUserId) {
    return new IssueException("User not found", Map.of("userId", requestedUserId));
  }

  public static IssueException invalidProject(String requestedProjectId) {
    return new IssueException("Project not found", Map.of("projectId", requestedProjectId));
  }

  public static IssueException invalidProjectVersion(String projectId, String version) {
    return new IssueException(
        "Version not found", Map.of("projectId", projectId, "version", version));
  }

  public static IssueException invalidSummary(String requestedSummary) {
    return new IssueException("Summary is invalid", Map.of("summary", requestedSummary));
  }

  public static IssueException invalidDescription(String requestedDescription) {
    return new IssueException(
        "Description is invalid", Map.of("description", requestedDescription));
  }

  public static IssueException invalidIssue(String issueId) {
    return new IssueException("Issue not found", Map.of("issueId", issueId));
  }

  public static IssueException cannotUpdateIssueEnd(String issueId) {
    return new IssueException(
        "Issue has been ended so can't be update", Map.of("issueId", issueId));
  }
}
