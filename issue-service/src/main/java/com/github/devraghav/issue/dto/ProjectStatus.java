package com.github.devraghav.issue.dto;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

public enum ProjectStatus {
  UNKNOWN(-1),
  POC(0),
  IN_PROGRESS(1),
  DEPLOYED(2);
  @Getter private int value;

  private static Map<Integer, ProjectStatus> reverseLookup =
      Arrays.stream(ProjectStatus.values())
          .collect(Collectors.toUnmodifiableMap(ProjectStatus::getValue, Function.identity()));

  ProjectStatus(int value) {
    this.value = value;
  }

  public static ProjectStatus fromValue(int value) {
    return reverseLookup.getOrDefault(value, ProjectStatus.UNKNOWN);
  }
}
