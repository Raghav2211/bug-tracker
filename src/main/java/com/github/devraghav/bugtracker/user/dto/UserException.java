package com.github.devraghav.bugtracker.user.dto;

import java.util.Map;
import lombok.Getter;

public class UserException extends RuntimeException {

  @Getter private final Map<String, Object> meta;

  private UserException(String message) {
    super(message);
    this.meta = Map.of();
  }

  private UserException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  public static UserException nullFirstName() {
    return new UserException("Firstname should not be null");
  }

  public static UserException nullLastName() {
    return new UserException("Lastname should not be null");
  }

  public static UserException invalidEmail(String email) {
    return new UserException("Invalid email", Map.of("email", email));
  }

  public static UserException notFound(String userId) {
    return new UserException("User not found", Map.of("userId", userId));
  }
}
