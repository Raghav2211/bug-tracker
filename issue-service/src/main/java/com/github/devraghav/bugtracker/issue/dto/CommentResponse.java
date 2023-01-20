package com.github.devraghav.bugtracker.issue.dto;

import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;
import lombok.AccessLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class CommentResponse {

  public static record Comment(
      String id,
      String issueId,
      String userId,
      String content,
      LocalDateTime createdAt,
      LocalDateTime lastUpdatedAt)
      implements Comparable<Comment> {

    @Override
    public int compareTo(Comment comment) {
      return comment.createdAt.compareTo(this.createdAt);
    }
  }

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Error {

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

  public static Mono<ServerResponse> retrieve(List<Comment> comments) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(comments));
  }

  public static Mono<ServerResponse> create(ServerRequest request, IssueResponse.Issue issue) {
    return ServerResponse.created(URI.create(request.path() + "/" + issue.id()))
        .body(BodyInserters.fromValue(issue));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                Error.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, CommentException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                Error.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }
}
