package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class IssueCommentRequest {
  private String userId;
  private String content;

  @JsonIgnore
  public boolean isContentValid() {
    return StringUtils.hasLength(this.content) && this.content.length() <= 256;
  }
}
