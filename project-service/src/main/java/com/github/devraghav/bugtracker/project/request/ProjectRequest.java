package com.github.devraghav.bugtracker.project.request;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

public final class ProjectRequest {

  public enum ProjectStatus {
    UNKNOWN(-1),
    POC(0),
    IN_PROGRESS(1),
    DEPLOYED(2);
    @Getter private final int value;

    private static final Map<Integer, ProjectStatus> VALUE_TO_STATUS_LOOKUP =
        Arrays.stream(ProjectStatus.values())
            .collect(Collectors.toUnmodifiableMap(ProjectStatus::getValue, Function.identity()));

    ProjectStatus(int value) {
      this.value = value;
    }

    public static ProjectStatus fromValue(int value) {
      return VALUE_TO_STATUS_LOOKUP.getOrDefault(value, ProjectStatus.UNKNOWN);
    }
  }

  public static record CreateProjectRequest(
      String name, String description, ProjectStatus status, Map<String, Object> tags) {
    public CreateProjectRequest {
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  public static record CreateVersionRequest(String version) {}
}
