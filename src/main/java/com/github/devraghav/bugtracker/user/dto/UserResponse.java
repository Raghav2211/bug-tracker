package com.github.devraghav.bugtracker.user.dto;

import com.github.devraghav.bugtracker.user.repository.UserAlreadyExistsException;
import com.github.devraghav.bugtracker.user.repository.UserNotFoundException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class UserResponse {

  public static Mono<ServerResponse> create(ServerRequest request, User user) {
    return ServerResponse.created(URI.create(request.path() + "/" + user.getId()))
        .body(BodyInserters.fromValue(user));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                UserErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> alreadyExists(
      ServerRequest request, UserAlreadyExistsException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                UserErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }

  public static Mono<ServerResponse> found(User user) {
    return ServerResponse.ok().body(BodyInserters.fromValue(user));
  }

  public static Mono<ServerResponse> notFound(
      ServerRequest request, UserNotFoundException exception) {
    return ServerResponse.status(HttpStatus.NOT_FOUND)
        .body(
            BodyInserters.fromValue(
                UserErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.NOT_FOUND,
                    exception.getMeta())));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, UserException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                UserErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }
}
