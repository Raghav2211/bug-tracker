package com.github.devraghav.project.dto;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class ProjectResponse {

  public static Mono<ServerResponse> retrieve(List<Project> projects) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(projects));
  }

  public static Mono<ServerResponse> create(ServerRequest request, Project project) {
    return ServerResponse.created(URI.create(request.path() + "/" + project.getId()))
        .body(BodyInserters.fromValue(project));
  }

  public static Mono<ServerResponse> ok(ProjectVersion projectVersion) {
    return ServerResponse.ok().body(BodyInserters.fromValue(projectVersion));
  }

  public static Mono<ServerResponse> noBody(ServerRequest request) {
    return ServerResponse.badRequest()
        .body(
            BodyInserters.fromValue(
                ProjectErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, ProjectException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                ProjectErrorResponse.of(
                    request.path(),
                    exception.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    exception.getMeta())));
  }

  public static Mono<ServerResponse> alreadyExists(
      ServerRequest request, ProjectException exception) {
    return exception(request, exception, HttpStatus.BAD_REQUEST);
  }

  public static Mono<ServerResponse> notFound(ServerRequest request, ProjectException exception) {
    return exception(request, exception, HttpStatus.NOT_FOUND);
  }

  public static Mono<ServerResponse> exception(
      ServerRequest request, ProjectException exception, HttpStatus httpStatus) {
    return ServerResponse.status(httpStatus)
        .body(
            BodyInserters.fromValue(
                ProjectErrorResponse.of(
                    request.path(), exception.getMessage(), httpStatus, exception.getMeta())));
  }
}
