package com.github.devraghav.bugtracker.issue.entity;

import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInfoRef {
  private String projectId;
  private String versionId;

  public ProjectInfoRef(IssueRequest.ProjectInfo projectInfo) {
    this.projectId = projectInfo.projectId();
    this.versionId = projectInfo.versionId();
  }
}
