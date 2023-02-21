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

  public static sealed interface User {
    String firstName();

    String lastName();

    Role role();
  }

  public static record CreateUser(
      String firstName, String lastName, String email, String password, Role role) implements User {
    public Role role() {
      return Objects.isNull(role) ? Role.ROLE_READ : role;
    }
  }

  public static record UpdateUser(String firstName, String lastName, Role role) implements User {}

  public enum Role {
    ROLE_ADMIN(0),
    ROLE_READ(1),
    ROLE_WRITE(2);
    @Getter private final int value;
    private static Map<Integer, Role> VALUE_TO_ENUM =
        Arrays.stream(Role.values())
            .collect(Collectors.toUnmodifiableMap(Role::getValue, Function.identity()));

    Role(int value) {
      this.value = value;
    }

    public static Role fromValue(int value) {
      return VALUE_TO_ENUM.getOrDefault(value, Role.ROLE_READ);
    }
  }
}
