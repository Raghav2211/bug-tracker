package com.github.devraghav.bugtracker.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.devraghav.bugtracker.user.exception.UserException;
import com.github.devraghav.bugtracker.user.request.UserRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class UserResponse {

  public record User(
      String id,
      UserRequest.Role role,
      String firstName,
      String lastName,
      String email,
      @JsonIgnore String password,
      Boolean enabled) {}

  public record Error(
      Integer status,
      String path,
      String errorMessage,
      LocalDateTime timeStamp,
      Map<String, Object> meta) {

    public static Error of(String path, String errorMessage, HttpStatus httpStatus) {
      return new Error(httpStatus.value(), path, errorMessage, LocalDateTime.now(), Map.of());
    }

    public static Error of(
        String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
      return new Error(httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
    }
  }

  public static Mono<ServerResponse> create(ServerRequest request, User user) {
    return ServerResponse.created(URI.create(request.path() + "/" + user.id()))
        .body(BodyInserters.fromValue(user));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                Error.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> found(User user) {
    return ServerResponse.ok().body(BodyInserters.fromValue(user));
  }

  public static Mono<ServerResponse> unauthorizedAccess(
      ServerRequest request, UserException exception) {
    return userException(request, exception, HttpStatus.UNAUTHORIZED);
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
                Error.of(request.path(), exception.getMessage(), httpStatus, exception.getMeta())));
  }
}
