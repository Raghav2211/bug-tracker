package com.github.devraghav.bugtracker.project.service;

import java.util.Map;
import lombok.Getter;

public class ProjectNotFoundException extends RuntimeException {
  private static final String MESSAGE = "Project not found";
  @Getter private final Map<String, Object> meta;

  public ProjectNotFoundException(String projectId) {
    super(MESSAGE);
    this.meta = Map.of("projectId", projectId);
  }
}
