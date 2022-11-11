package com.github.devraghav.bugtracker.project.entity;

import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectEntity {

  private String id;
  private String name;
  private String description;
  private Boolean enabled = true;
  private Integer status;
  private Map<String, Object> tags;
  private String author;
  private LocalDateTime createdAt;

  public ProjectEntity(ProjectRequest projectRequest) {
    this.id = UUID.randomUUID().toString();
    this.name = projectRequest.getName();
    this.description = projectRequest.getDescription();
    this.enabled = true;
    this.status = projectRequest.getStatus().getValue();
    this.tags = projectRequest.getTags();
    this.author = projectRequest.getAuthor();
    this.createdAt = LocalDateTime.now();
  }
}
