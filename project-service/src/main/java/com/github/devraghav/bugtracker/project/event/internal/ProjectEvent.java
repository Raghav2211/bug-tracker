package com.github.devraghav.bugtracker.project.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.project.response.ProjectResponse;
import lombok.Getter;

public interface ProjectEvent {
  @Getter
  class Created extends DomainEvent {
    private final ProjectResponse.Project project;

    public Created(ProjectResponse.Project project) {
      super(
          project.id(),
          "Created",
          new PublisherInfo("Project", ProjectResponse.Project.class, project.author()));
      this.project = project;
    }
  }

  @Getter
  class VersionCreated extends DomainEvent {
    private final String projectId;
    private final ProjectResponse.VersionResponse version;

    public VersionCreated(String projectId, ProjectResponse.VersionResponse version) {
      super(
          projectId,
          "Created",
          new PublisherInfo("Project", ProjectResponse.VersionResponse.class, version.userId()));
      this.projectId = projectId;
      this.version = version;
    }
  }
}
