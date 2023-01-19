package com.github.devraghav.bugtracker.project.event;

import com.github.devraghav.bugtracker.project.dto.RequestResponse;
import com.github.devraghav.bugtracker.project.event.internal.ProjectEvent;
import com.github.devraghav.data_model.event.project.version.VersionCreated;
import com.github.devraghav.data_model.schema.project.version.VersionCreatedSchema;
import java.time.ZoneOffset;

class VersionCreatedEventConverter
    implements EventConverter<ProjectEvent.VersionCreated, VersionCreatedSchema> {

  private com.github.devraghav.data_model.domain.project.version.Version getVersion(
      RequestResponse.VersionResponse version) {
    return com.github.devraghav.data_model.domain.project.version.Version.newBuilder()
        .setId(version.id())
        .setVersion(version.version())
        .build();
  }

  @Override
  public VersionCreatedSchema convert(ProjectEvent.VersionCreated event) {
    return VersionCreatedSchema.newBuilder()
        .setEvent(
            VersionCreated.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setProjectId(event.getProjectId())
                .setPayload(getVersion(event.getVersion()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
