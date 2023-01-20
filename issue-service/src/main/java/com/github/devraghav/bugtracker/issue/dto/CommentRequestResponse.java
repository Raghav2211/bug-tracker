package com.github.devraghav.bugtracker.issue.dto;

import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class CommentRequestResponse {

  public static record CreateCommentRequest(String userId, String issueId, String content) {}

  public static record UpdateCommentRequest(
      String userId, String issueId, String commentId, String content) {}

  public static record CommentResponse(
      String id,
      String issueId,
      String userId,
      String content,
      LocalDateTime createdAt,
      LocalDateTime lastUpdatedAt)
      implements Comparable<CommentResponse> {

    @Override
    public int compareTo(CommentResponse comment) {
      return comment.createdAt().compareTo(this.createdAt);
    }
  }

  @Data
  @AllArgsConstructor
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ErrorResponse {

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

  public static Mono<ServerResponse> retrieve(Collection<CommentResponse> comments) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(comments));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                ErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, CommentException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                ErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }
}
