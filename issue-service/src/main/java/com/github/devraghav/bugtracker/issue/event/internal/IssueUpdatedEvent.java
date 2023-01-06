package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class IssueUpdatedEvent extends DomainEvent {
  private final Issue updatedIssue;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public IssueUpdatedEvent(Issue updatedIssue) {
    super("Updated", publisherInfo);
    this.updatedIssue = updatedIssue;
  }
}
