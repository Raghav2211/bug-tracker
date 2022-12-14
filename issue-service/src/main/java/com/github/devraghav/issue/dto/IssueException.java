package com.github.devraghav.issue.dto;

import java.util.HashMap;
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

  public static IssueException invalidUser(String userId) {
    return new IssueException("User not found", Map.of("userId", userId));
  }

  public static IssueException invalidProject(String projectId) {
    return new IssueException("Project not found", Map.of("projectId", projectId));
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

  public static IssueException alreadyEnded(String issueId) {
    return new IssueException("Issue has been ended", Map.of("issueId", issueId));
  }

  public static IssueException noProjectAttach() {
    return new IssueException("No project attached");
  }

  public static IssueException invalidProject(ProjectInfo projectInfo) {
    var meta = new HashMap<String, Object>();
    meta.put("projectId", projectInfo.getProjectId());
    meta.put("versionId", projectInfo.getVersionId());
    return new IssueException("Either projectId or versionId is invalid", meta);
  }
}
