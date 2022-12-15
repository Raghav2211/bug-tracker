package com.github.devraghav.project.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record Project(
    String id,
    String name,
    String description,
    Boolean enabled,
    ProjectStatus status,
    User author,
    LocalDateTime createdAt,
    Set<ProjectVersion> versions) {

  public void removeProjectVersionIfNotEqual(String versionId) {
    versions.removeIf(version -> !version.id().equals(versionId));
  }
}
