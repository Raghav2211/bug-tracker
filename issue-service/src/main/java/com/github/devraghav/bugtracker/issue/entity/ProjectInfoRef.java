package com.github.devraghav.bugtracker.issue.entity;

import com.github.devraghav.bugtracker.issue.dto.IssueRequestResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInfoRef {
  private String projectId;
  private String versionId;

  public ProjectInfoRef(IssueRequestResponse.ProjectInfo projectInfo) {
    this.projectId = projectInfo.projectId();
    this.versionId = projectInfo.versionId();
  }
}
