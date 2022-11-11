package com.github.devraghav.bugtracker.project.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@NoArgsConstructor
public class ProjectRequest {
  private String name;
  private String description;
  private ProjectStatus status;
  private String author;
  private Map<String, Object> tags = new HashMap<>();

  @JsonIgnore
  public boolean isDescriptionValid() {
    return StringUtils.hasLength(this.description) && this.description.length() <= 20;
  }

  @JsonIgnore
  public boolean isAuthorNotNull() {
    return StringUtils.hasLength(this.author);
  }

  @JsonIgnore
  public boolean isNameValid() {
    return StringUtils.hasLength(this.name) && this.getName().matches("^[a-zA-Z]*$");
  }
}
