package com.github.devraghav.bugtracker.project.dto;

import java.util.Map;

public interface ProjectRequest {

  record Create(String name, String description, ProjectStatus status, Map<String, Object> tags) {
    public Create {
      tags = Map.copyOf(tags == null ? Map.of() : tags);
    }
  }

  record CreateVersion(String version) {}
}
