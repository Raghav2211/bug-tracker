package com.github.devraghav.bugtracker.issue.dto;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Severity {
  UNKNOWN(-1),
  LOW(0),
  MEDIUM(1),
  HIGH(2);

  private static Map<Integer, Severity> reverseLookup =
      Arrays.stream(Severity.values())
          .collect(Collectors.toUnmodifiableMap(Severity::getValue, Function.identity()));

  private int value;

  Severity(int value) {
    this.value = value;
  }

  public static Severity fromValue(int value) {
    return reverseLookup.getOrDefault(value, Severity.UNKNOWN);
  }
}
