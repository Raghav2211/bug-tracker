package com.github.devraghav.user.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

public record User(
    String id, AccessLevel access, String firstName, String lastName, String email, Boolean enabled)
    implements Serializable {

  @JsonIgnore
  public boolean isWriteAccess() {
    return this.access == AccessLevel.ADMIN || this.access == AccessLevel.WRITE;
  }
}
