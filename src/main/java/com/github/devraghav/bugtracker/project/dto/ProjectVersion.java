package com.github.devraghav.bugtracker.project.dto;

import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectVersion {
  private String id;
  private String version;

  public ProjectVersion(ProjectVersionEntity projectVersionEntity) {
    this.id = projectVersionEntity.getId();
    this.version = projectVersionEntity.getVersion();
  }
}
