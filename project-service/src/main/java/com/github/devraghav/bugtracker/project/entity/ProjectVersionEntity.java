package com.github.devraghav.bugtracker.project.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectVersionEntity {
  private String id;
  private String version;
  private LocalDateTime createdAt;
  private String createdBy;
}
