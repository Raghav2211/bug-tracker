package com.github.devraghav.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record User(
    String id,
    AccessLevel access,
    String firstName,
    String lastName,
    String email,
    Boolean enabled) {

  @JsonIgnore
  public boolean isWriteAccess() {
    return this.access == AccessLevel.ADMIN || this.access == AccessLevel.WRITE;
  }
}