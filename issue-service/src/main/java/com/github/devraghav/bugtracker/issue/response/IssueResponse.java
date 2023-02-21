package com.github.devraghav.bugtracker.issue.response;

import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class IssueResponse {

  public record ProjectAttachment(String projectId, String name, Version version) {}

  public record Version(String id, String version) {}

  public static record Issue(
      String id,
      Priority priority,
      Severity severity,
      String businessUnit,
      Set<ProjectAttachment> attachments,
      String header,
      String description,
      String assignee,
      String reporter,
      Set<String> watchers,
      Map<String, String> tags,
      LocalDateTime createdAt,
      LocalDateTime endedAt) {}

  public static record Error(
      int status,
      String path,
      String errorMessage,
      LocalDateTime timeStamp,
      Map<String, Object> meta) {
    public Error {
      meta = Map.copyOf(meta == null ? Map.of() : meta);
    }

    public static Error of(String path, String errorMessage, HttpStatus httpStatus) {
      return new Error(httpStatus.value(), path, errorMessage, LocalDateTime.now(), Map.of());
    }

    public static Error of(
        String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
      return new Error(httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
    }
  }

  @Getter
  public enum Priority {
    UNKNOWN(-1),
    LOW(0),
    NORMAL(1),
    HIGH(2),
    URGENT(3);
    private static final Map<Integer, IssueResponse.Priority> VALUE_TO_ENUM_LOOKUP =
        Arrays.stream(IssueResponse.Priority.values())
            .collect(
                Collectors.toUnmodifiableMap(
                    IssueResponse.Priority::getValue, Function.identity()));

    private final int value;

    Priority(int value) {
      this.value = value;
    }

    public static IssueResponse.Priority fromValue(int value) {
      return VALUE_TO_ENUM_LOOKUP.getOrDefault(value, IssueResponse.Priority.UNKNOWN);
    }
  }

  @Getter
  public enum Severity {
    UNKNOWN(-1),
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    private static final Map<Integer, IssueResponse.Severity> VALUE_TO_ENUM_LOOKUP =
        Arrays.stream(IssueResponse.Severity.values())
            .collect(
                Collectors.toUnmodifiableMap(
                    IssueResponse.Severity::getValue, Function.identity()));

    private final int value;

    Severity(int value) {
      this.value = value;
    }

    public static IssueResponse.Severity fromValue(int value) {
      return VALUE_TO_ENUM_LOOKUP.getOrDefault(value, IssueResponse.Severity.UNKNOWN);
    }
  }

  public static <E, T extends org.springframework.data.domain.Page<E>>
      Mono<ServerResponse> retrieve(T pageable) {

    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(pageable));
  }

  public static Mono<ServerResponse> create(ServerRequest request, IssueResponse.Issue issue) {
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
                Error.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, IssueException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                Error.of(
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
                Error.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.NOT_FOUND,
                    exception.getMeta())));
  }
}
