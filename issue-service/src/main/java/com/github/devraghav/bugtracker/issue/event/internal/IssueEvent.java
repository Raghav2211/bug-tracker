package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;

public interface IssueEvent {

  @Getter
  class Created extends DomainEvent {
    private final Issue createdIssue;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

    public Created(Issue createdIssue) {
      super("Created", publisherInfo);
      this.createdIssue = createdIssue;
    }
  }

  @Getter
  class Updated extends DomainEvent {
    private final Issue updatedIssue;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

    public Updated(Issue updatedIssue) {
      super("Updated", publisherInfo);
      this.updatedIssue = updatedIssue;
    }
  }

  @Getter
  class Assigned extends DomainEvent {
    private final String issueId;
    private final User assignee;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

    public Assigned(String issueId, User assignee) {
      super("Assigned", publisherInfo);
      this.issueId = issueId;
      this.assignee = assignee;
    }
  }

  @Getter
  class Unassigned extends DomainEvent {
    private final String issueId;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

    public Unassigned(String issueId) {
      super("Unassigned", publisherInfo);
      this.issueId = issueId;
    }
  }

  @Getter
  class WatchStarted extends DomainEvent {
    private final String issueId;
    private final User watcher;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

    public WatchStarted(String issueId, User watcher) {
      super("WatchStarted", publisherInfo);
      this.issueId = issueId;
      this.watcher = watcher;
    }
  }

  @Getter
  class WatchEnded extends DomainEvent {
    private final String issueId;
    private final User watchEndedBy;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

    public WatchEnded(String issueId, User watchEndedBy) {
      super("WatchStarted", publisherInfo);
      this.issueId = issueId;
      this.watchEndedBy = watchEndedBy;
    }
  }

  @Getter
  class Resolved extends DomainEvent {
    private final String issueId;
    private final LocalDateTime resolvedAt;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Issue.class);

    public Resolved(String issueId, LocalDateTime resolvedAt) {
      super("Resolved", publisherInfo);
      this.issueId = issueId;
      this.resolvedAt = resolvedAt;
    }
  }

  @Getter
  class CommentAdded extends DomainEvent {
    private final String issueId;
    private final Comment comment;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Comment.class);

    public CommentAdded(String issueId, Comment comment) {
      super("Created", publisherInfo);
      this.issueId = issueId;
      this.comment = comment;
    }
  }

  @Getter
  class CommentUpdated extends DomainEvent {
    private final String issueId;
    private final Comment comment;

    @Getter(AccessLevel.NONE)
    private static final PublisherInfo publisherInfo = new PublisherInfo("Issue", Comment.class);

    public CommentUpdated(String issueId, Comment comment) {
      super("Updated", publisherInfo);
      this.issueId = issueId;
      this.comment = comment;
    }
  }
}
