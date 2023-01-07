package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class CommentAddedEvent extends DomainEvent {
  private final String issueId;
  private final Comment comment;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Comment.class);

  public CommentAddedEvent(String issueId, Comment comment) {
    super("Created", publisherInfo);
    this.issueId = issueId;
    this.comment = comment;
  }
}
