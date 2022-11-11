package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class ProjectInfo {
  private String projectId;
  private String versionId;

  @JsonIgnore
  public boolean isValid() {
    return StringUtils.hasLength(projectId) && StringUtils.hasLength(versionId);
  }
}
