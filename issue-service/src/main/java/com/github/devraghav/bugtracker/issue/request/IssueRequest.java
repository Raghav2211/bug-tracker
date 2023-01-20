package com.github.devraghav.bugtracker.issue.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

public final class IssueRequest {

  public record ProjectInfo(String projectId, String versionId) {

    @JsonIgnore
    public boolean isValid() {
      return StringUtils.hasLength(projectId) && StringUtils.hasLength(versionId);
    }
  }

  public enum MonitorType {
    ASSIGN,
    UNASSIGN,
    WATCH,
    UNWATCH;
  }

  public static record CreateIssue(
      IssueResponse.Priority priority,
      IssueResponse.Severity severity,
      String businessUnit,
      Set<ProjectInfo> projects,
      String header,
      String description,
      Map<String, String> tags) {
    public CreateIssue {
      projects = Set.copyOf(projects == null ? Set.of() : projects);
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  public static record UpdateIssue(
      IssueResponse.Priority priority,
      IssueResponse.Severity severity,
      String businessUnit,
      String header,
      String description,
      Map<String, String> tags) {
    public UpdateIssue {
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  public static record Page(Integer page, Integer size, Sort sort) {

    public static Page of(ServerRequest request) {
      return new Page(
          request.queryParam("page").map(Integer::parseInt).orElseGet(() -> 0),
          request.queryParam("size").map(Integer::parseInt).orElseGet(() -> 10),
          request.queryParam("sort").map(Sort::by).orElseGet(() -> Sort.by("createdAt")));
    }
  }

  public static record Monitor(
      String issueId, String user, MonitorType monitorType, String requestedBy) {}
}
