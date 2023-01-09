package com.github.devraghav.bugtracker.project.event;

import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.User;
import com.github.devraghav.bugtracker.project.dto.Version;
import com.github.devraghav.bugtracker.project.event.internal.ProjectCreatedEvent;
import com.github.devraghav.data_model.event.project.ProjectCreated;
import com.github.devraghav.data_model.schema.project.ProjectCreatedSchema;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectCreatedEventConverter
    implements EventConverter<ProjectCreatedEvent, ProjectCreatedSchema> {

  private com.github.devraghav.data_model.domain.user.User getUser(User user) {
    return com.github.devraghav.data_model.domain.user.User.newBuilder()
        .setId(user.id())
        .setAccessLevel(user.access().name())
        .setEmail(user.email())
        .setEnabled(user.enabled())
        .setFirstName(user.firstName())
        .setLastName(user.lastName())
        .build();
  }

  private List<com.github.devraghav.data_model.domain.project.version.Version> getVersions(
      Set<Version> versions) {
    return versions.stream().map(this::getVersion).collect(Collectors.toList());
  }

  private com.github.devraghav.data_model.domain.project.version.Version getVersion(
      Version version) {
    return com.github.devraghav.data_model.domain.project.version.Version.newBuilder()
        .setId(version.id())
        .setVersion(version.version())
        .build();
  }

  private com.github.devraghav.data_model.domain.project.Project getProject(Project project) {
    var tags =
        project.getTags().entrySet().stream()
            .collect(Collectors.toMap(Objects::toString, Object::toString));
    return com.github.devraghav.data_model.domain.project.Project.newBuilder()
        .setId(project.getId())
        .setName(project.getName())
        .setAuthor(getUser(project.getAuthor()))
        .setDescription(project.getDescription())
        .setEnabled(project.getEnabled())
        .setCreatedAt(project.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
        .setTags(tags)
        .setStatus(project.getStatus().name())
        .setVersions(getVersions(project.getVersions()))
        .build();
  }

  @Override
  public ProjectCreatedSchema convert(ProjectCreatedEvent event) {
    return ProjectCreatedSchema.newBuilder()
        .setEvent(
            ProjectCreated.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getProject(event.getProject()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
