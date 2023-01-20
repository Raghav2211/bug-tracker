package com.github.devraghav.bugtracker.user.request;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

public final class UserRequest {

  public static record AuthRequest(String email, String password) {}

  public static record AuthResponse(String token) {}

  public static record CreateUser(
      String firstName, String lastName, String email, String password, Role role) {
    public Role role() {
      return Objects.isNull(role) ? Role.ROLE_READ : role;
    }
  }

  public enum Role {
    ROLE_ADMIN(0),
    ROLE_READ(1),
    ROLE_WRITE(2);
    @Getter private int value;
    private static Map<Integer, Role> reverseLookup =
        Arrays.stream(Role.values())
            .collect(Collectors.toUnmodifiableMap(Role::getValue, Function.identity()));

    Role(int value) {
      this.value = value;
    }

    public static Role fromValue(int value) {
      return reverseLookup.getOrDefault(value, Role.ROLE_READ);
    }
  }
}
