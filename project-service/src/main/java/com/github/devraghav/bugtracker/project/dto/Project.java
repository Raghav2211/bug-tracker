package com.github.devraghav.bugtracker.project.dto;

import com.github.devraghav.bugtracker.user.dto.User;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Project {
  private String id;
  private String name;
  private String description;
  private Boolean enabled;
  private ProjectStatus status;
  private User author;
  private LocalDateTime createdAt;
  private Set<ProjectVersion> versions;

  public static class ProjectBuilder {
    public ProjectBuilder() {}
  }
}
