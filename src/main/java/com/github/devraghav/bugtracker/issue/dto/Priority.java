package com.github.devraghav.bugtracker.issue.dto;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum Priority {
  UNKNOWN(-1),
  LOW(0),
  NORMAL(1),
  HIGH(2),
  URGENT(3);
  private static Map<Integer, Priority> reverseLookup =
      Arrays.stream(Priority.values())
          .collect(Collectors.toUnmodifiableMap(Priority::getValue, Function.identity()));

  private int value;

  Priority(int value) {
    this.value = value;
  }

  public static Priority fromValue(int value) {
    return reverseLookup.getOrDefault(value, Priority.UNKNOWN);
  }
}
