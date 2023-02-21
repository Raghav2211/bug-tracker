package com.github.devraghav.bugtracker.issue.project;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

public final class ProjectResponse {

  public static enum ProjectStatus {
    UNKNOWN(-1),
    POC(0),
    IN_PROGRESS(1),
    DEPLOYED(2);
    @Getter private final int value;

    private static final Map<Integer, ProjectStatus> VALUE_TO_ENUM =
        Arrays.stream(ProjectStatus.values())
            .collect(Collectors.toUnmodifiableMap(ProjectStatus::getValue, Function.identity()));

    ProjectStatus(int value) {
      this.value = value;
    }

    public static ProjectStatus fromValue(int value) {
      return VALUE_TO_ENUM.getOrDefault(value, ProjectStatus.UNKNOWN);
    }
  }

  public record Project(
      String id, String name, Boolean enabled, ProjectStatus status, Set<Version> versions) {
    public record Version(String id, String version) {}
  }
}
