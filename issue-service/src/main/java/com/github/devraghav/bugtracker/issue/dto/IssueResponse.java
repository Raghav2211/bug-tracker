package com.github.devraghav.bugtracker.issue.dto;

import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class IssueResponse {

  public static <E, T extends Page<E>> Mono<ServerResponse> retrieve(T pageable) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(pageable));
  }

  public static Mono<ServerResponse> create(ServerRequest request, Issue issue) {
    return ServerResponse.created(URI.create(request.path() + "/" + issue.getId()))
        .body(BodyInserters.fromValue(issue));
  }

  public static Mono<ServerResponse> noContent() {
    return ServerResponse.noContent().build();
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                IssueErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, IssueException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                IssueErrorResponse.of(
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
                IssueErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.NOT_FOUND,
                    exception.getMeta())));
  }
}
