package com.github.devraghav.bugtracker.user.service;

import java.util.Map;
import lombok.Getter;

public class UserAlreadyExistsException extends RuntimeException {

  private static String EMAIL_MESSAGE = "User already exists";
  @Getter private final Map<String, Object> meta;

  private UserAlreadyExistsException(Map<String, Object> meta) {
    super(EMAIL_MESSAGE);
    this.meta = meta;
  }

  public static UserAlreadyExistsException withEmail(String email) {
    return new UserAlreadyExistsException(Map.of("email", email));
  }
}
