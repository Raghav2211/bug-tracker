package com.github.devraghav.bugtracker.issue.exception;

import java.util.Map;
import lombok.Getter;
import org.springframework.web.reactive.function.client.WebClientRequestException;

public class UserClientException extends RuntimeException {
  @Getter private final Map<String, Object> meta;

  private UserClientException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  public static UserClientException invalidUser(String userId) {
    return new UserClientException("User not found", Map.of("userId", userId));
  }

  public static UserClientException unableToConnect(WebClientRequestException exception) {
    return new UserClientException(
        "Unable to connect with Project Service", Map.of("path", exception.getUri().getPath()));
  }
}
