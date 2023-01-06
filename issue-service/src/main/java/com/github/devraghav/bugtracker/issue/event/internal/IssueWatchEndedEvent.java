package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.User;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class IssueWatchEndedEvent extends DomainEvent {
  private final String issueId;
  private final User watchEndedBy;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public IssueWatchEndedEvent(String issueId, User watchEndedBy) {
    super("WatchStarted", publisherInfo);
    this.issueId = issueId;
    this.watchEndedBy = watchEndedBy;
  }
}
