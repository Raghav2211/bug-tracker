package com.github.devraghav.bugtracker.user.dto;

public interface LoginRequest {
  record Request(String email, String password) {}

  record Response(String token) {}
}
