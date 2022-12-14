package com.github.devraghav.issue.dto;

import com.github.devraghav.issue.entity.IssueEntity;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import lombok.AccessLevel;

@Data
@Builder(builderMethodName = "")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Issue {

  private String id;

  private Priority priority;
  private Severity severity;
  private String businessUnit;
  private List<Project> projects;

  private String header;
  private String description;

  private User assignee;

  private User reporter;

  @Singular private Set<User> watchers;

  private List<IssueComment> comments;
  private Map<String, String> tags = new HashMap<>();

  private LocalDateTime createdAt;

  private LocalDateTime endedAt;

  public static IssueBuilder builder(IssueEntity issueEntity) {
    return new IssueBuilder()
        .id(issueEntity.getId())
        .priority(Priority.fromValue(issueEntity.getPriority()))
        .severity(Severity.fromValue(issueEntity.getSeverity()))
        .businessUnit(issueEntity.getBusinessUnit())
        .header(issueEntity.getHeader())
        .description(issueEntity.getDescription())
        .tags(issueEntity.getTags())
        .createdAt(issueEntity.getCreatedAt())
        .endedAt(issueEntity.getEndedAt());
  }

  public static class IssueBuilder {
    private List<Project> projects;
    private List<IssueComment> comments;

    public IssueBuilder projects(List<Project> projects) {
      this.projects = projects;
      projects.forEach(this::addWatcher);
      return this;
    }

    public IssueBuilder comments(List<IssueComment> comments) {
      this.comments =
          comments.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
      return this;
    }

    /**
     * This will add project author as a default watcher of this issue
     *
     * @param project
     */
    private void addWatcher(Project project) {
      this.watcher(project.getAuthor());
    }
  }
}
