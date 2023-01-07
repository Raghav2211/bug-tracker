package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.User;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class AssignedEvent extends DomainEvent {
  private final String issueId;
  private final User assignee;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public AssignedEvent(String issueId, User assignee) {
    super("Assigned", publisherInfo);
    this.issueId = issueId;
    this.assignee = assignee;
  }
}
