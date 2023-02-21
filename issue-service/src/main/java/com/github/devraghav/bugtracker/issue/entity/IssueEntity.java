package com.github.devraghav.bugtracker.issue.entity;

import java.time.LocalDateTime;
import java.util.*;
import lombok.*;
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
  private String header;
  private String description;
  private String reporter;
  private String assignee;
  private Set<ProjectAttachment> attachments;
  private Set<String> watchers;
  private Map<String, String> tags;
  private LocalDateTime createdAt;
  private LocalDateTime endedAt;
  private String lastUpdateBy;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder
  public static class ProjectAttachment {
    private String projectId;
    private String name;
    private Version version;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder
  public static class Version {
    private String id;
    private String version;
  }
}
