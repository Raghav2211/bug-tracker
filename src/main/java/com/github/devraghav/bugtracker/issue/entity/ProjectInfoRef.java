package com.github.devraghav.bugtracker.issue.entity;

import com.github.devraghav.bugtracker.issue.dto.ProjectInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInfoRef {
  private String projectId;
  private String versionId;

  public ProjectInfoRef(ProjectInfo projectInfo) {
    this.projectId = projectInfo.getProjectId();
    this.versionId = projectInfo.getVersionId();
  }
}
