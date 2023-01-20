package com.github.devraghav.bugtracker.issue.exception;

import java.util.Map;
import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClientRequestException;

public class ProjectClientException extends RuntimeException {
  @Getter private final Map<String, Object> meta;

  private ProjectClientException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  public static ProjectClientException invalidProject(String projectId) {
    return new ProjectClientException("Project not found", Map.of("projectId", projectId));
  }

  public static ProjectClientException invalidProjectOrVersion(String projectId, String versionId) {
    return new ProjectClientException(
        "Either project or version not found",
        Map.of("projectId", projectId, "versionId", versionId));
  }

  public static ProjectClientException unableToConnect(WebClientRequestException exception) {
    return new ProjectClientException(
        "Unable to connect with Project Service", Map.of("path", exception.getUri().getPath()));
  }
}
