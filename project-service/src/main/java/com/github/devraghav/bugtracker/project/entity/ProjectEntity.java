package com.github.devraghav.bugtracker.project.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "project")
@Data
@NoArgsConstructor
public class ProjectEntity {

  @Id private String id;
  private String name;
  private String description;
  private Boolean enabled = true;
  private Integer status;
  private Map<String, Object> tags;
  private String author;
  private List<ProjectVersionEntity> versions;
  private LocalDateTime createdAt;
}
