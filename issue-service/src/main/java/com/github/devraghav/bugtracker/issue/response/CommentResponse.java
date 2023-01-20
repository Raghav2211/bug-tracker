package com.github.devraghav.bugtracker.issue.response;

import com.github.devraghav.bugtracker.issue.excpetion.CommentException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
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
      return comment.createdAt().compareTo(this.createdAt);
    }
  }

  public static record Error(
      int status,
      String path,
      String errorMessage,
      LocalDateTime timeStamp,
      Map<String, Object> meta) {
    public Error {
      meta = Map.copyOf(meta == null ? Map.of() : meta);
    }

    public static IssueResponse.Error of(String path, String errorMessage, HttpStatus httpStatus) {
      return new IssueResponse.Error(
          httpStatus.value(), path, errorMessage, LocalDateTime.now(), Map.of());
    }

    public static IssueResponse.Error of(
        String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
      return new IssueResponse.Error(
          httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
    }
  }

  public static Mono<ServerResponse> retrieve(Collection<Comment> comments) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(comments));
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
