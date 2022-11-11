package com.github.devraghav.bugtracker.project.repository;

import java.util.Map;
import lombok.Getter;

public class ProjectAlreadyExistsException extends RuntimeException {

  private static String EMAIL_MESSAGE = "Project already exists";
  @Getter private final Map<String, Object> meta;

  private ProjectAlreadyExistsException(Map<String, Object> meta) {
    super(EMAIL_MESSAGE);
    this.meta = meta;
  }

  public static ProjectAlreadyExistsException withName(String name) {
    return new ProjectAlreadyExistsException(Map.of("name", name));
  }
}
