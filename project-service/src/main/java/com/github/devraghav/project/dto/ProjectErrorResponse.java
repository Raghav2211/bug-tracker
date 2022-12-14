package com.github.devraghav.project.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectErrorResponse {

  private final int status;
  private final String path;
  private final String errorMessage;
  private final LocalDateTime timeStamp;

  private Map<String, Object> meta = new HashMap<>();

  public static ProjectErrorResponse of(String path, String errorMessage, HttpStatus httpStatus) {
    return new ProjectErrorResponse(httpStatus.value(), path, errorMessage, LocalDateTime.now());
  }

  public static ProjectErrorResponse of(
      String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
    return new ProjectErrorResponse(
        httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
  }
}
