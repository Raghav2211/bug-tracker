package com.github.devraghav.bugtracker.user.repository;

import java.util.Map;
import lombok.Getter;

public class UserNotFoundException extends RuntimeException {
  private static final String MESSAGE = "User not found";
  @Getter private final Map<String, Object> meta;

  public UserNotFoundException(String userId) {
    super(MESSAGE);
    this.meta = Map.of("userId", userId);
  }
}
