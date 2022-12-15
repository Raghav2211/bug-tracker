package com.github.devraghav.project.dto;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum AccessLevel {
  ADMIN(0),
  READ(1),
  WRITE(2);
  @Getter private int value;
  private static Map<Integer, AccessLevel> reverseLookup =
      Arrays.stream(AccessLevel.values())
          .collect(Collectors.toUnmodifiableMap(AccessLevel::getValue, Function.identity()));

  AccessLevel(int value) {
    this.value = value;
  }

  public static AccessLevel fromValue(int value) {
    return reverseLookup.getOrDefault(value, AccessLevel.READ);
  }
}
