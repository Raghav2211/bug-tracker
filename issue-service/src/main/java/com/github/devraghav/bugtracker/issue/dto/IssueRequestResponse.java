package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class IssueRequestResponse {

  public record ProjectInfo(String projectId, String versionId) {

    @JsonIgnore
    public boolean isValid() {
      return StringUtils.hasLength(projectId) && StringUtils.hasLength(versionId);
    }
  }

  public static record CreateIssueRequest(
      Priority priority,
      Severity severity,
      String businessUnit,
      Set<ProjectInfo> projects,
      String header,
      String description,
      Map<String, String> tags) {
    public CreateIssueRequest {
      projects = Set.copyOf(projects == null ? Set.of() : projects);
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  public static record UpdateIssueRequest(
      Priority priority,
      Severity severity,
      String businessUnit,
      String header,
      String description,
      Map<String, String> tags) {
    public UpdateIssueRequest {
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

  public static record AssignRequest(
      String issueId, String user, MonitorType monitorType, String requestedBy) {}

  public static record IssueResponse(
      String id,
      Priority priority,
      Severity severity,
      String businessUnit,
      Set<ProjectInfo> projects,
      String header,
      String description,
      String assignee,
      String reporter,
      Set<String> watchers,
      Map<String, String> tags,
      LocalDateTime createdAt,
      LocalDateTime endedAt) {}

  public enum MonitorType {
    ASSIGN,
    UNASSIGN,
    WATCH,
    UNWATCH;
  }

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ErrorResponse {

    private final int status;
    private final String path;
    private final String errorMessage;
    private final LocalDateTime timeStamp;

    private Map<String, Object> meta = new HashMap<>();

    public static ErrorResponse of(String path, String errorMessage, HttpStatus httpStatus) {
      return new ErrorResponse(httpStatus.value(), path, errorMessage, LocalDateTime.now());
    }

    public static ErrorResponse of(
        String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
      return new ErrorResponse(httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
    }
  }

  @Getter
  public enum Priority {
    UNKNOWN(-1),
    LOW(0),
    NORMAL(1),
    HIGH(2),
    URGENT(3);
    private static final Map<Integer, Priority> VALUE_TO_ENUM_LOOKUP =
        Arrays.stream(Priority.values())
            .collect(Collectors.toUnmodifiableMap(Priority::getValue, Function.identity()));

    private final int value;

    Priority(int value) {
      this.value = value;
    }

    public static Priority fromValue(int value) {
      return VALUE_TO_ENUM_LOOKUP.getOrDefault(value, Priority.UNKNOWN);
    }
  }

  @Getter
  public enum Severity {
    UNKNOWN(-1),
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    private static final Map<Integer, Severity> VALUE_TO_ENUM_LOOKUP =
        Arrays.stream(Severity.values())
            .collect(Collectors.toUnmodifiableMap(Severity::getValue, Function.identity()));

    private final int value;

    Severity(int value) {
      this.value = value;
    }

    public static Severity fromValue(int value) {
      return VALUE_TO_ENUM_LOOKUP.getOrDefault(value, Severity.UNKNOWN);
    }
  }

  public static <E, T extends org.springframework.data.domain.Page<E>>
      Mono<ServerResponse> retrieve(T pageable) {

    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(pageable));
  }

  public static Mono<ServerResponse> create(ServerRequest request, IssueResponse issue) {
    return ServerResponse.created(URI.create(request.path() + "/" + issue.id()))
        .body(BodyInserters.fromValue(issue));
  }

  public static Mono<ServerResponse> noContent() {
    return ServerResponse.noContent().build();
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                ErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, IssueException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                ErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }

  public static Mono<ServerResponse> notFound(
      ServerRequest request, IssueNotFoundException exception) {
    return ServerResponse.status(HttpStatus.NOT_FOUND)
        .body(
            BodyInserters.fromValue(
                ErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.NOT_FOUND,
                    exception.getMeta())));
  }
}
