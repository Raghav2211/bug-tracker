package com.github.devraghav.bugtracker.project.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.Version;
import lombok.AccessLevel;
import lombok.Getter;

public interface ProjectEvent {
  @Getter
  class Created extends DomainEvent {
    private final Project project;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Project", Project.class);

    public Created(Project project) {
      super("Created", publisherInfo);
      this.project = project;
    }
  }

  @Getter
  class VersionCreated extends DomainEvent {
    private final String projectId;
    private final Version version;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Project", Version.class);

    public VersionCreated(String projectId, Version version) {
      super("Created", publisherInfo);
      this.projectId = projectId;
      this.version = version;
    }
  }
}
