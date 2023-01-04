package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

public interface CommentRequest {
  String content();

  @JsonIgnore
  default boolean isContentValid() {
    var content = content();
    return StringUtils.hasLength(content) && content.length() <= 256;
  }
}
