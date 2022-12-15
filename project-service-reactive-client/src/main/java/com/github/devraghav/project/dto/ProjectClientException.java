package com.github.devraghav.project.dto;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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
    return new ProjectClientException("Either project or version not found", Map.of("projectId", projectId,"versionId", versionId
    ));
  }

}
