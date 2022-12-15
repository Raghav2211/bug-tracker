package com.github.devraghav.issue.entity;

import com.github.devraghav.issue.dto.ProjectInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInfoRef {
  private String projectId;
  private String versionId;

  public ProjectInfoRef(ProjectInfo projectInfo) {
    this.projectId = projectInfo.projectId();
    this.versionId = projectInfo.versionId();
  }
}
