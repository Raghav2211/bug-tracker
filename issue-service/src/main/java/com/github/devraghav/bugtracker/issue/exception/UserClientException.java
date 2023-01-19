package com.github.devraghav.bugtracker.issue.exception;

import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Slf4j
public class UserClientException extends RuntimeException {
  @Getter private final Map<String, Object> meta;

  private UserClientException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  private UserClientException(String message) {
    this(message, Map.of());
  }

  public static UserClientException invalidUser(String userId) {
    return new UserClientException("User not found", Map.of("userId", userId));
  }

  public static UserClientException unableToConnect(WebClientRequestException exception) {
    log.error("Unable to connect with User Service {} ", exception.getUri(), exception);
    return new UserClientException(
        "Unable to connect with User Service", Map.of("path", exception.getUri().getPath()));
  }
}
