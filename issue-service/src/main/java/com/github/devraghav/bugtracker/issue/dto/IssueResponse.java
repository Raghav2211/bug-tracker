package com.github.devraghav.bugtracker.issue.dto;

import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import com.github.devraghav.data_model.domain.issue.Issue;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.*;
import lombok.AccessLevel;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface IssueResponse {

  static <E, T extends Page<E>> Mono<ServerResponse> retrieve(T pageable) {

    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(pageable));
  }

  static Mono<ServerResponse> create(ServerRequest request, Issue issue) {
    return ServerResponse.created(URI.create(request.path() + "/" + issue.id()))
        .body(BodyInserters.fromValue(issue));
  }

  static Mono<ServerResponse> noContent() {
    return ServerResponse.noContent().build();
  }

  static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                Error.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  static Mono<ServerResponse> invalid(ServerRequest request, IssueException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                Error.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }

  static Mono<ServerResponse> notFound(ServerRequest request, IssueNotFoundException exception) {
    return ServerResponse.status(HttpStatus.NOT_FOUND)
        .body(
            BodyInserters.fromValue(
                Error.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.NOT_FOUND,
                    exception.getMeta())));
  }

  record Issue(
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

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  class Error {

    private final int status;
    private final String path;
    private final String errorMessage;
    private final LocalDateTime timeStamp;

    private Map<String, Object> meta = new HashMap<>();

    public static Error of(String path, String errorMessage, HttpStatus httpStatus) {
      return new Error(httpStatus.value(), path, errorMessage, LocalDateTime.now());
    }

    public static Error of(
        String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
      return new Error(httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
    }
  }
}
