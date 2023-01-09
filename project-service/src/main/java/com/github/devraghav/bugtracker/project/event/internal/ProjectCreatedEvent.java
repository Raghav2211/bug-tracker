package com.github.devraghav.bugtracker.project.event.internal;

import com.github.devraghav.bugtracker.project.dto.Project;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class ProjectCreatedEvent extends DomainEvent {
  private final Project project;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Project", Project.class);

  public ProjectCreatedEvent(Project project) {
    super("Created", publisherInfo);
    this.project = project;
  }
}
