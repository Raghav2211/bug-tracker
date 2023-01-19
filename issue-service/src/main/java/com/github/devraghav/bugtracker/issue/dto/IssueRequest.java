package com.github.devraghav.bugtracker.issue.dto;

import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.web.reactive.function.server.ServerRequest;

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
      Map<String, String> tags) {
    public Update {
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  record Page(Integer page, Integer size, Sort sort) {

    public static Page of(ServerRequest request) {
      return new Page(
          request.queryParam("page").map(Integer::parseInt).orElseGet(() -> 0),
          request.queryParam("size").map(Integer::parseInt).orElseGet(() -> 10),
          request.queryParam("sort").map(Sort::by).orElseGet(() -> Sort.by("createdAt")));
    }
  }

  record Assign(String issueId, String user, MonitorType monitorType, String requestedBy) {}

  record CreateComment(String userId, String issueId, String content) {}

  record UpdateComment(String userId, String issueId, String commentId, String content) {}
}
