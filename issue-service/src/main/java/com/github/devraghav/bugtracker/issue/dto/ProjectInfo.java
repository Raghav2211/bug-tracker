package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

public record ProjectInfo(String projectId, String versionId) {

  @JsonIgnore
  public boolean isValid() {
    return StringUtils.hasLength(projectId) && StringUtils.hasLength(versionId);
  }
}
