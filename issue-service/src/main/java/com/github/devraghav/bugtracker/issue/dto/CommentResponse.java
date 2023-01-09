package com.github.devraghav.bugtracker.issue.dto;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class CommentResponse {

  public static Mono<ServerResponse> retrieve(List<Comment> comments) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(comments));
  }

  public static Mono<ServerResponse> create(ServerRequest request, Issue issue) {
    return ServerResponse.created(URI.create(request.path() + "/" + issue.getId()))
        .body(BodyInserters.fromValue(issue));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                CommentErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, CommentException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                CommentErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }
}
