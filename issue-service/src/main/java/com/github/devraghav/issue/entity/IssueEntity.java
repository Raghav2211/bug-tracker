package com.github.devraghav.issue.entity;

import java.time.LocalDateTime;
import java.util.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("issues")
@Data
@NoArgsConstructor
public class IssueEntity {
  @Id private String id;
  private Integer priority;
  private Integer severity;
  private String businessUnit;

  private Set<ProjectInfoRef> projects;

  private String header;

  private String description;
  private String reporter;

  private String assignee;

  private Set<String> watchers;

  private Map<String, String> tags;

  private LocalDateTime createdAt;
  private LocalDateTime endedAt;

  public Optional<String> getAssignee() {
    return Optional.ofNullable(assignee);
  }
}
