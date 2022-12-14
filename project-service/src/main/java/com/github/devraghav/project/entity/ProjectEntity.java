package com.github.devraghav.project.entity;

import com.github.devraghav.project.dto.ProjectRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "project")
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
  private List<ProjectVersionEntity> versions;
  private LocalDateTime createdAt;

  public ProjectEntity(ProjectRequest projectRequest) {
    this.id = UUID.randomUUID().toString();
    this.name = projectRequest.getName();
    this.description = projectRequest.getDescription();
    this.enabled = true;
    this.status = projectRequest.getStatus().getValue();
    this.tags = projectRequest.getTags();
    this.author = projectRequest.getAuthor();
    this.versions = new ArrayList<>();
    this.createdAt = LocalDateTime.now();
  }
}
