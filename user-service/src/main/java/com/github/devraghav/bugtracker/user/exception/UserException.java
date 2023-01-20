package com.github.devraghav.bugtracker.user.exception;

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

  public static class UnauthorizedException extends UserException {

    private UnauthorizedException(Map<String, Object> meta) {
      super("Unauthorized access", meta);
    }
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

  public static UserException alreadyExistsWithEmail(String email) {
    return new UserException("User already exists", Map.of("email", email));
  }

  public static UserException notFoundById(String userId) {
    return new UserException("User not found", Map.of("userId", userId));
  }

  public static UserException notFoundByEmail(String email) {
    return new UserException("User not found", Map.of("email", email));
  }

  public static UserException unauthorizedAccess(String email) {
    return new UnauthorizedException(Map.of("email", email));
  }

  public static UserException unknownRole() {
    return new UserException("Unrecognized role");
  }
}
