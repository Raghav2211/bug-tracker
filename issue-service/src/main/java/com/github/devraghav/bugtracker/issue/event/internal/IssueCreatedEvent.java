package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class IssueCreatedEvent extends DomainEvent {
  private final Issue createdIssue;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public IssueCreatedEvent(Issue createdIssue) {
    super("Created", publisherInfo);
    this.createdIssue = createdIssue;
  }
}
