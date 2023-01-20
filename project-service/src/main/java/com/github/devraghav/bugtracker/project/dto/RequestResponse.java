package com.github.devraghav.bugtracker.project.dto;

import com.github.devraghav.bugtracker.project.exception.ProjectException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class RequestResponse {

  public enum ProjectStatus {
    UNKNOWN(-1),
    POC(0),
    IN_PROGRESS(1),
    DEPLOYED(2);
    @Getter private final int value;

    private static final Map<Integer, ProjectStatus> VALUE_TO_STATUS_LOOKUP =
        Arrays.stream(ProjectStatus.values())
            .collect(Collectors.toUnmodifiableMap(ProjectStatus::getValue, Function.identity()));

    ProjectStatus(int value) {
      this.value = value;
    }

    public static ProjectStatus fromValue(int value) {
      return VALUE_TO_STATUS_LOOKUP.getOrDefault(value, ProjectStatus.UNKNOWN);
    }
  }

  public static record CreateProjectRequest(
      String name, String description, ProjectStatus status, Map<String, Object> tags) {
    public CreateProjectRequest {
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  public static record CreateVersionRequest(String version) {}

  public static record ErrorResponse(
      Integer status,
      String path,
      String errorMessage,
      LocalDateTime timeStamp,
      Map<String, Object> meta) {

    public static ErrorResponse of(String path, String errorMessage, HttpStatus httpStatus) {
      return new ErrorResponse(
          httpStatus.value(), path, errorMessage, LocalDateTime.now(), Map.of());
    }

    public static ErrorResponse of(
        String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
      return new ErrorResponse(httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
    }
  }

  public static record ProjectResponse(
      String id,
      String name,
      String description,
      Boolean enabled,
      ProjectStatus status,
      String author,
      LocalDateTime createdAt,
      Set<VersionResponse> versions,
      Map<String, Object> tags) {}

  public static record VersionResponse(String id, String version, String userId) {}

  public static Mono<ServerResponse> retrieve(List<ProjectResponse> projects) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(projects));
  }

  public static Mono<ServerResponse> create(ServerRequest request, ProjectResponse project) {
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
                ErrorResponse.of(request.path(), "Body not found", HttpStatus.BAD_REQUEST)));
  }

  public static Mono<ServerResponse> invalid(ServerRequest request, ProjectException exception) {
    return ServerResponse.status(HttpStatus.BAD_REQUEST)
        .body(
            BodyInserters.fromValue(
                ErrorResponse.of(
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
                ErrorResponse.of(
                    request.path(), exception.getMessage(), httpStatus, exception.getMeta())));
  }
}
