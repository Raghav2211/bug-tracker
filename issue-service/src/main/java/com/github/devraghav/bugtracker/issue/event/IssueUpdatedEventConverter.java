package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.dto.IssueResponse;
import com.github.devraghav.bugtracker.issue.dto.ProjectInfo;
import com.github.devraghav.bugtracker.issue.dto.ProjectVersion;
import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.domain.issue.ProjectAttachment;
import com.github.devraghav.data_model.domain.project.version.Version;
import com.github.devraghav.data_model.event.issue.IssueUpdated;
import com.github.devraghav.data_model.schema.issue.IssueUpdatedSchema;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class IssueUpdatedEventConverter implements EventConverter<IssueEvent.Updated, IssueUpdatedSchema> {

  private ProjectAttachment getProject(ProjectInfo project) {
    return ProjectAttachment.newBuilder()
        .setProjectId(project.projectId())
        .setProjectVersionId(project.versionId())
        .build();
  }

  private Version getVersion(ProjectVersion version) {
    return Version.newBuilder().setId(version.id()).setVersion(version.version()).build();
  }

  private List<Version> getVersions(Set<ProjectVersion> versions) {
    return versions.stream().map(this::getVersion).collect(Collectors.toList());
  }

  private List<ProjectAttachment> getProjects(Set<ProjectInfo> projects) {
    return projects.stream().map(this::getProject).collect(Collectors.toList());
  }

  private com.github.devraghav.data_model.domain.issue.Issue getIssue(IssueResponse.Issue issue) {
    var issueBuilder =
        com.github.devraghav.data_model.domain.issue.Issue.newBuilder()
            .setId(issue.id())
            .setHeader(issue.header())
            .setDescription(issue.description())
            .setBusinessUnit(issue.businessUnit())
            .setPriority(issue.priority().name())
            .setSeverity(issue.severity().name())
            .setAssignee(issue.assignee())
            .setProjects(getProjects(issue.projects()))
            .setWatchers(List.copyOf(issue.watchers()))
            .setReporter(issue.reporter())
            .setTags(issue.tags())
            .setCreatedAt(issue.createdAt().toEpochSecond(ZoneOffset.UTC));
    if (issue.endedAt() != null) {
      issueBuilder.setEndedAt(issue.endedAt().toEpochSecond(ZoneOffset.UTC));
    }
    return issueBuilder.build();
  }

  @Override
  public IssueUpdatedSchema convert(IssueEvent.Updated event) {
    return IssueUpdatedSchema.newBuilder()
        .setEvent(
            IssueUpdated.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getIssue(event.getUpdatedIssue()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
