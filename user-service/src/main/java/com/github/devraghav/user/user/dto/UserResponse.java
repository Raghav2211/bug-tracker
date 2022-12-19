package com.github.devraghav.user.user.dto;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class UserResponse {

  public static Mono<ServerResponse> create(ServerRequest request, User user) {
    return ServerResponse.created(URI.create(request.path() + "/" + user.id()))
        .body(BodyInserters.fromValue(user));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                UserErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> found(User user) {
    return ServerResponse.ok().body(BodyInserters.fromValue(user));
  }

  public static Mono<ServerResponse> notFound(ServerRequest request, UserException exception) {
    return userException(request, exception, HttpStatus.NOT_FOUND);
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, UserException exception) {
    return userException(request, exception, HttpStatus.BAD_REQUEST);
  }

  private static Mono<ServerResponse> userException(
      ServerRequest request, UserException exception, HttpStatus httpStatus) {
    return ServerResponse.status(httpStatus)
        .body(
            BodyInserters.fromValue(
                UserErrorResponse.of(
                    request.path(), exception.getMessage(), httpStatus, exception.getMeta())));
  }
}
