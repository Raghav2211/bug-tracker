package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.User;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class IssueWatchStartedEvent extends DomainEvent {
  private final String issueId;
  private final User watcher;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public IssueWatchStartedEvent(String issueId, User watcher) {
    super("WatchEnded", publisherInfo);
    this.issueId = issueId;
    this.watcher = watcher;
  }
}
