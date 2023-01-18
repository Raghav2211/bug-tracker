package com.github.devraghav.bugtracker.project.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.Version;
import lombok.Getter;

public interface ProjectEvent {
  @Getter
  class Created extends DomainEvent {
    private final Project project;

    public Created(Project project) {
      super(
          project.getId(),
          "Created",
          new PublisherInfo("Project", Project.class, project.getAuthor().id()));
      this.project = project;
    }
  }

  @Getter
  class VersionCreated extends DomainEvent {
    private final String projectId;
    private final Version version;

    public VersionCreated(String userId, String projectId, Version version) {
      super(projectId, "Created", new PublisherInfo("Project", Version.class, userId));
      this.projectId = projectId;
      this.version = version;
    }
  }
}
