package com.github.devraghav.bugtracker.user.dto;

import java.util.Objects;

public record CreateUserRequest(
    String firstName, String lastName, String email, String password, AccessLevel access) {
  @Override
  public AccessLevel access() {
    return Objects.isNull(access) ? AccessLevel.READ : access;
  }
}
