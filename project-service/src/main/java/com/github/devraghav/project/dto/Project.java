package com.github.devraghav.project.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Project {
  private String id;
  private String name;
  private String description;
  private Boolean enabled;
  private ProjectStatus status;
  private User author;
  private LocalDateTime createdAt;
  private Set<ProjectVersion> versions;
}
