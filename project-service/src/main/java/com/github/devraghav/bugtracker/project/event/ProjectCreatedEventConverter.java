package com.github.devraghav.bugtracker.project.event;

import com.github.devraghav.bugtracker.project.dto.ProjectResponse;
import com.github.devraghav.bugtracker.project.event.internal.ProjectEvent;
import com.github.devraghav.data_model.event.project.ProjectCreated;
import com.github.devraghav.data_model.schema.project.ProjectCreatedSchema;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectCreatedEventConverter
    implements EventConverter<ProjectEvent.Created, ProjectCreatedSchema> {

  private List<com.github.devraghav.data_model.domain.project.version.Version> getVersions(
      Set<ProjectResponse.Version> versions) {
    return versions.stream().map(this::getVersion).collect(Collectors.toList());
  }

  private com.github.devraghav.data_model.domain.project.version.Version getVersion(
      ProjectResponse.Version version) {
    return com.github.devraghav.data_model.domain.project.version.Version.newBuilder()
        .setId(version.id())
        .setVersion(version.version())
        .build();
  }

  private com.github.devraghav.data_model.domain.project.Project getProject(
      ProjectResponse.Project project) {
    var tags =
        project.tags().entrySet().stream()
            .collect(Collectors.toMap(Objects::toString, Object::toString));
    return com.github.devraghav.data_model.domain.project.Project.newBuilder()
        .setId(project.id())
        .setName(project.name())
        .setAuthor(project.author())
        .setDescription(project.description())
        .setEnabled(project.enabled())
        .setCreatedAt(project.createdAt().toEpochSecond(ZoneOffset.UTC))
        .setTags(tags)
        .setStatus(project.status().name())
        .setVersions(getVersions(project.versions()))
        .build();
  }

  @Override
  public ProjectCreatedSchema convert(ProjectEvent.Created event) {
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
