package com.github.devraghav.bugtracker.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record User(
    String id,
    Role role,
    String firstName,
    String lastName,
    String email,
    @JsonIgnore String password,
    Boolean enabled) {

  @JsonIgnore
  public boolean hasWriteAccess() {
    return this.role == Role.ROLE_ADMIN || this.role == Role.ROLE_WRITE;
  }
}
