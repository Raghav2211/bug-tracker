package com.github.devraghav.bugtracker.project.dto;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;

public record ProjectErrorResponse(
    Integer status,
    String path,
    String errorMessage,
    LocalDateTime timeStamp,
    Map<String, Object> meta) {

  public static ProjectErrorResponse of(String path, String errorMessage, HttpStatus httpStatus) {
    return new ProjectErrorResponse(
        httpStatus.value(), path, errorMessage, LocalDateTime.now(), Map.of());
  }

  public static ProjectErrorResponse of(
      String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
    return new ProjectErrorResponse(
        httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
  }
}
