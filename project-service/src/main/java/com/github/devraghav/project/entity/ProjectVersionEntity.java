package com.github.devraghav.project.entity;

import com.github.devraghav.project.dto.ProjectVersionRequest;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectVersionEntity {
  private String id;
  private String version;

  public ProjectVersionEntity(ProjectVersionRequest projectVersionRequest) {
    this.id = UUID.randomUUID().toString();
    this.version = projectVersionRequest.getVersion();
  }
}
