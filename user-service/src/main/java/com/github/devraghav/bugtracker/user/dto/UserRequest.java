package com.github.devraghav.bugtracker.user.dto;

import java.util.Objects;

public interface UserRequest {
  record Create(String firstName, String lastName, String email, String password, Role role) {
    public Role role() {
      return Objects.isNull(role) ? Role.ROLE_READ : role;
    }
  }
}
