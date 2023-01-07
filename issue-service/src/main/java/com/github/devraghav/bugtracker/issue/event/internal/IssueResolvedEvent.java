package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Issue;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class IssueResolvedEvent extends DomainEvent {
  private final String issueId;
  private final LocalDateTime resolvedAt;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public IssueResolvedEvent(String issueId, LocalDateTime resolvedAt) {
    super("Resolved", publisherInfo);
    this.issueId = issueId;
    this.resolvedAt = resolvedAt;
  }
}
