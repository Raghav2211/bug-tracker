package com.github.devraghav.user.dto;

import java.util.Map;
import lombok.Getter;

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
}
