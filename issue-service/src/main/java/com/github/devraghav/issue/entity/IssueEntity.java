package com.github.devraghav.issue.entity;

import com.github.devraghav.issue.dto.IssueRequest;
import com.github.devraghav.issue.dto.IssueUpdateRequest;
import com.github.devraghav.issue.dto.Priority;
import com.github.devraghav.issue.dto.Severity;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
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

  private Set<String> watchers = new HashSet<>();

  private Map<String, String> tags = new HashMap<>();

  private LocalDateTime createdAt;
  private LocalDateTime endedAt;

  public Optional<String> getAssignee() {
    return Optional.ofNullable(assignee);
  }

  public IssueEntity(IssueRequest issueRequest) {
    this.id = UUID.randomUUID().toString();
    this.priority = issueRequest.getPriority().getValue();

    this.severity = issueRequest.getSeverity().getValue();
    this.businessUnit = issueRequest.getBusinessUnit();
    this.projects =
        issueRequest.getProjects().stream().map(ProjectInfoRef::new).collect(Collectors.toSet());
    this.header = issueRequest.getHeader();

    this.description = issueRequest.getDescription();
    if (!issueRequest.getTags().isEmpty()) {
      this.tags = issueRequest.getTags();
    }
    this.reporter = issueRequest.getReporter();
    this.createdAt = LocalDateTime.now();
  }

  public IssueEntity(IssueEntity issueEntity, IssueUpdateRequest request) {
    this.id = issueEntity.getId();
    this.priority = request.getPriority().map(Priority::getValue).orElse(issueEntity.getPriority());
    this.severity = request.getSeverity().map(Severity::getValue).orElse(issueEntity.getSeverity());
    this.businessUnit = request.getBusinessUnit().orElse(issueEntity.getBusinessUnit());
    this.projects = issueEntity.getProjects();
    this.header = request.getHeader().orElse(issueEntity.getHeader());
    this.description = request.getDescription().orElse(issueEntity.getDescription());
    this.tags = request.getTags().orElse(issueEntity.getTags());
    this.reporter = issueEntity.getReporter();
    this.createdAt = issueEntity.getCreatedAt();
    this.endedAt = issueEntity.getEndedAt();
  }
}
