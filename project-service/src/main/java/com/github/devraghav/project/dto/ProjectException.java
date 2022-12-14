package com.github.devraghav.project.dto;

import java.util.Map;
import lombok.Getter;

public class ProjectException extends RuntimeException {
  @Getter private final Map<String, Object> meta;

  private ProjectException(String message) {
    super(message);
    this.meta = Map.of();
  }

  private ProjectException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  public static ProjectException nullAuthor() {
    return new ProjectException("Author should not be null");
  }

  public static ProjectException invalidName(String name) {
    return new ProjectException("Name is invalid", Map.of("name", name));
  }

  public static ProjectException invalidDescription(String description) {
    return new ProjectException("Description is invalid", Map.of("description", description));
  }

  public static ProjectException authorNotFound(String author) {
    return new ProjectException("Author not found", Map.of("author", author));
  }

  public static ProjectException authorNotHaveWriteAccess(String author) {
    return new ProjectException("Author don't have write access", Map.of("author", author));
  }

  public static ProjectException notFound(String projectId) {
    return new ProjectException("Project not found", Map.of("projectId", projectId));
  }

  public static ProjectException alreadyExistsByName(String name) {
    return new ProjectException("Project already exists", Map.of("name", name));
  }
}
