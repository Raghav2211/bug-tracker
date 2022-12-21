package com.github.devraghav.bugtracker.project.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectVersionEntity {
  private String id;
  private String version;
}
