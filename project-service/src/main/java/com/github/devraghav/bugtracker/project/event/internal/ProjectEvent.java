package com.github.devraghav.bugtracker.project.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.project.dto.RequestResponse;
import lombok.Getter;

public interface ProjectEvent {
  @Getter
  class Created extends DomainEvent {
    private final RequestResponse.ProjectResponse project;

    public Created(RequestResponse.ProjectResponse project) {
      super(
          project.id(),
          "Created",
          new PublisherInfo("Project", RequestResponse.ProjectResponse.class, project.author()));
      this.project = project;
    }
  }

  @Getter
  class VersionCreated extends DomainEvent {
    private final String projectId;
    private final RequestResponse.VersionResponse version;

    public VersionCreated(String projectId, RequestResponse.VersionResponse version) {
      super(
          projectId,
          "Created",
          new PublisherInfo("Project", RequestResponse.VersionResponse.class, version.userId()));
      this.projectId = projectId;
      this.version = version;
    }
  }
}
