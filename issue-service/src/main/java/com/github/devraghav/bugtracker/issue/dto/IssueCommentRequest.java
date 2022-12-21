package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

public record IssueCommentRequest(String userId, String content) {

  @JsonIgnore
  public boolean isContentValid() {
    return StringUtils.hasLength(this.content) && this.content.length() <= 256;
  }
}
