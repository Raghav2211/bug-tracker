package com.github.devraghav.bugtracker.issue.excpetion;

import com.github.devraghav.bugtracker.issue.dto.IssueRequestResponse;
import com.github.devraghav.bugtracker.issue.exception.ProjectClientException;
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

  public static IssueException invalidSummary(String requestedSummary) {
    return new IssueException("Summary is invalid", Map.of("summary", requestedSummary));
  }

  public static IssueException invalidDescription(String requestedDescription) {
    return new IssueException(
        "Description is invalid", Map.of("description", requestedDescription));
  }

  public static IssueException projectServiceException(
      ProjectClientException projectClientException) {
    return new IssueException(
        projectClientException.getMessage(), projectClientException.getMeta());
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

  public static IssueException invalidProject(IssueRequestResponse.ProjectInfo projectInfo) {
    var meta = new HashMap<String, Object>();
    meta.put("projectId", projectInfo.projectId());
    meta.put("versionId", projectInfo.versionId());
    return new IssueException("Either projectId or versionId is invalid", meta);
  }
}
