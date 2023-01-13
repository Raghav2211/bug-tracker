package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.Project;
import com.github.devraghav.bugtracker.issue.dto.ProjectVersion;
import com.github.devraghav.bugtracker.issue.event.internal.IssueEvents;
import com.github.devraghav.data_model.domain.project.version.Version;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.IssueCreated;
import com.github.devraghav.data_model.schema.issue.IssueCreatedSchema;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class IssueCreatedEventConverter
    implements EventConverter<IssueEvents.Created, IssueCreatedSchema> {

  private User getUser(com.github.devraghav.bugtracker.issue.dto.User author) {
    return User.newBuilder()
        .setId(author.id())
        .setAccessLevel(author.access().name())
        .setEmail(author.email())
        .setEnabled(author.enabled())
        .setFirstName(author.firstName())
        .setLastName(author.lastName())
        .build();
  }

  private com.github.devraghav.data_model.domain.project.Project getProject(Project project) {
    return com.github.devraghav.data_model.domain.project.Project.newBuilder()
        .setId(project.id())
        .setName(project.name())
        .setAuthor(getUser(project.author()))
        .setDescription(project.description())
        .setEnabled(project.enabled())
        .setCreatedAt(project.createdAt().toEpochSecond(ZoneOffset.UTC))
        .setStatus(project.status().name())
        .setVersions(getVersions(project.versions()))
        .build();
  }

  private Version getVersion(ProjectVersion version) {
    return Version.newBuilder().setId(version.id()).setVersion(version.version()).build();
  }

  private List<Version> getVersions(Set<ProjectVersion> versions) {
    return versions.stream().map(this::getVersion).collect(Collectors.toList());
  }

  private User getAssignee(com.github.devraghav.bugtracker.issue.dto.User assignee) {
    return assignee == null ? null : getUser(assignee);
  }

  private List<User> getWatchers(Set<com.github.devraghav.bugtracker.issue.dto.User> watchers) {
    return watchers == null
        ? null
        : watchers.stream().map(this::getUser).collect(Collectors.toList());
  }

  private List<com.github.devraghav.data_model.domain.project.Project> getProjects(
      List<Project> projects) {
    return projects.stream().map(this::getProject).collect(Collectors.toList());
  }

  private com.github.devraghav.data_model.domain.issue.Issue getIssue(Issue issue) {
    var issueBuilder =
        com.github.devraghav.data_model.domain.issue.Issue.newBuilder()
            .setId(issue.getId())
            .setHeader(issue.getHeader())
            .setDescription(issue.getDescription())
            .setBusinessUnit(issue.getBusinessUnit())
            .setPriority(issue.getPriority().name())
            .setSeverity(issue.getSeverity().name())
            .setAssignee(getAssignee(issue.getAssignee()))
            .setProjects(getProjects(issue.getProjects()))
            .setWatchers(getWatchers(issue.getWatchers()))
            .setReporter(getUser(issue.getReporter()))
            .setTags(issue.getTags())
            .setCreatedAt(issue.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
    if (issue.getEndedAt() != null) {
      issueBuilder.setEndedAt(issue.getEndedAt().toEpochSecond(ZoneOffset.UTC));
    }
    return issueBuilder.build();
  }

  @Override
  public IssueCreatedSchema convert(IssueEvents.Created event) {
    return IssueCreatedSchema.newBuilder()
        .setEvent(
            IssueCreated.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getIssue(event.getCreatedIssue()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
