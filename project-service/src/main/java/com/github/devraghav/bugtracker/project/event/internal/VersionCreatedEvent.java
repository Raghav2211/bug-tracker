package com.github.devraghav.bugtracker.project.event.internal;

import com.github.devraghav.bugtracker.project.dto.Version;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class VersionCreatedEvent extends DomainEvent {
  private final String projectId;
  private final Version version;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Project", Version.class);

  public VersionCreatedEvent(String projectId, Version version) {
    super("Created", publisherInfo);
    this.projectId = projectId;
    this.version = version;
  }
}
