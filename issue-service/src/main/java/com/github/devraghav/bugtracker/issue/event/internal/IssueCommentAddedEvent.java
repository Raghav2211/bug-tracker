package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.dto.Issue;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class IssueCommentAddedEvent extends DomainEvent {
  private final String issueId;
  private final Comment comment;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

  public IssueCommentAddedEvent(String issueId, Comment comment) {
    super("Created", publisherInfo);
    this.issueId = issueId;
    this.comment = comment;
  }
}
