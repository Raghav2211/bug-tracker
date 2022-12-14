package com.github.devraghav.bugtracker.project.dto;

import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.user.dto.User;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Project {
  private String id;
  private String name;
  private String description;
  private Boolean enabled;
  private ProjectStatus status;
  private User author;
  private LocalDateTime createdAt;
  private Set<ProjectVersion> versions = new HashSet<>();

  public Project(ProjectEntity entity, User author) {
    this.id = entity.getId();
    this.name = entity.getName();
    this.description = entity.getDescription();
    this.enabled = entity.getEnabled();
    this.status = ProjectStatus.fromValue(entity.getStatus());
    this.author = author;
    this.versions =
        entity.getVersions().stream().map(ProjectVersion::new).collect(Collectors.toSet());
    this.createdAt = entity.getCreatedAt();
  }

  public Project(ProjectEntity entity, User author, Set<ProjectVersion> versions) {
    this(entity, author);
    this.versions = versions;
  }

  public void removeProjectVersionIfNotEqual(String versionId) {
    versions.removeIf(version -> !version.getId().equals(versionId));
  }
}
