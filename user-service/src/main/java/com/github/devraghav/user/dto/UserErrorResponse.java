package com.github.devraghav.user.dto;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;

public record UserErrorResponse(
    Integer status,
    String path,
    String errorMessage,
    LocalDateTime timeStamp,
    Map<String, Object> meta) {

  public static UserErrorResponse of(String path, String errorMessage, HttpStatus httpStatus) {
    return new UserErrorResponse(
        httpStatus.value(), path, errorMessage, LocalDateTime.now(), Map.of());
  }

  public static UserErrorResponse of(
      String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
    return new UserErrorResponse(httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
  }
}
