package com.github.devraghav.bugtracker.project.response;

import com.github.devraghav.bugtracker.project.exception.ProjectException;
import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class ProjectResponse {

  public static record Project(
      String id,
      String name,
      String description,
      Boolean enabled,
      ProjectRequest.ProjectStatus status,
      String author,
      LocalDateTime createdAt,
      Set<VersionResponse> versions,
      Map<String, Object> tags) {}

  public static record Error(
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

  public static record VersionResponse(String id, String version, String userId) {}

  public static Mono<ServerResponse> retrieve(List<Project> projects) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(projects));
  }

  public static Mono<ServerResponse> create(ServerRequest request, Project project) {
    return ServerResponse.created(URI.create(request.path() + "/" + project.id()))
        .body(BodyInserters.fromValue(project));
  }

  public static Mono<ServerResponse> ok(VersionResponse version) {
    return ServerResponse.ok().body(BodyInserters.fromValue(version));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                Error.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, ProjectException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                Error.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }

  static Mono<ServerResponse> alreadyExists(ServerRequest request, ProjectException exception) {
    return exception(request, exception, HttpStatus.BAD_REQUEST);
  }

  public static Mono<ServerResponse> notFound(ServerRequest request, ProjectException exception) {
    return exception(request, exception, HttpStatus.NOT_FOUND);
  }

  static Mono<ServerResponse> exception(
      ServerRequest request, ProjectException exception, HttpStatus httpStatus) {
    return ServerResponse.status(httpStatus)
        .body(
            BodyInserters.fromValue(
                Error.of(request.path(), exception.getMessage(), httpStatus, exception.getMeta())));
  }
}
