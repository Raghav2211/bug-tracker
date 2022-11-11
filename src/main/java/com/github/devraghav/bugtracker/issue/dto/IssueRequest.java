package com.github.devraghav.bugtracker.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class IssueRequest {

  private Priority priority;
  private Severity severity;
  private String businessUnit;
  private Set<ProjectInfo> projects = new HashSet<>();
  private String header;
  private String description;
  private String reporter;

  private Map<String, String> tags = new HashMap<>();

  @JsonIgnore
  public boolean hasSeverity() {
    return this.severity != null;
  }

  @JsonIgnore
  public boolean hasPriority() {
    return this.priority != null;
  }

  @JsonIgnore
  public boolean isHeaderValid() {
    return StringUtils.hasText(this.header) && this.header.length() <= 50;
  }

  @JsonIgnore
  public boolean isDescriptionValid() {
    return StringUtils.hasText(this.description) && this.description.length() <= 200;
  }
}
