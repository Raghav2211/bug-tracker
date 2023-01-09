package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class IssueUnassignedEvent extends DomainEvent {
  private final String issueId;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public IssueUnassignedEvent(String issueId) {
    super("Unassigned", publisherInfo);
    this.issueId = issueId;
  }
}
