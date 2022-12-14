package com.github.devraghav.issue.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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

  public void removeProjectVersionIfNotEqual(String versionId) {
    versions.removeIf(version -> !version.getId().equals(versionId));
  }
}
