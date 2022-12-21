package com.github.devraghav.bugtracker.issue.dto;

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
public class IssueErrorResponse {

  private final int status;
  private final String path;
  private final String errorMessage;
  private final LocalDateTime timeStamp;

  private Map<String, Object> meta = new HashMap<>();

  public static IssueErrorResponse of(String path, String errorMessage, HttpStatus httpStatus) {
    return new IssueErrorResponse(httpStatus.value(), path, errorMessage, LocalDateTime.now());
  }

  public static IssueErrorResponse of(
      String path, String errorMessage, HttpStatus httpStatus, Map<String, Object> meta) {
    return new IssueErrorResponse(
        httpStatus.value(), path, errorMessage, LocalDateTime.now(), meta);
  }
}
