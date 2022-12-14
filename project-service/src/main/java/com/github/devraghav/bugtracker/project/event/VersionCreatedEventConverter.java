package com.github.devraghav.bugtracker.project.event;

import com.github.devraghav.bugtracker.project.dto.Version;
import com.github.devraghav.bugtracker.project.event.internal.VersionCreatedEvent;
import com.github.devraghav.data_model.event.project.version.VersionCreated;
import com.github.devraghav.data_model.schema.project.version.VersionCreatedSchema;
import java.time.ZoneOffset;

public class VersionCreatedEventConverter
    implements EventConverter<VersionCreatedEvent, VersionCreatedSchema> {

  private com.github.devraghav.data_model.domain.project.version.Version getVersion(
      Version version) {
    return com.github.devraghav.data_model.domain.project.version.Version.newBuilder()
        .setId(version.id())
        .setVersion(version.version())
        .build();
  }

  @Override
  public VersionCreatedSchema convert(VersionCreatedEvent event) {
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
