package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.bugtracker.issue.dto.User;
import java.time.LocalDateTime;
import lombok.Getter;

public interface IssueEvent {

  @Getter
  class Created extends DomainEvent {
    private final Issue createdIssue;

    public Created(Issue createdIssue) {
      super(
          createdIssue.getId(),
          "Created",
          new PublisherInfo("Issue", Issue.class, createdIssue.getReporter().id()));
      this.createdIssue = createdIssue;
    }
  }

  @Getter
  class Updated extends DomainEvent {
    private final Issue updatedIssue;

    public Updated(String updateBy, Issue updatedIssue) {
      super(updatedIssue.getId(), "Updated", new PublisherInfo("Issue", Issue.class, updateBy));
      this.updatedIssue = updatedIssue;
    }
  }

  @Getter
  class Assigned extends DomainEvent {
    private final String issueId;
    private final User assignee;

    public Assigned(String issueId, User assignee, String requestedBy) {
      super(issueId, "Assigned", new PublisherInfo("Issue", Issue.class, requestedBy));
      this.issueId = issueId;
      this.assignee = assignee;
    }
  }

  @Getter
  class Unassigned extends DomainEvent {
    private final String issueId;

    public Unassigned(String issueId, String requestedBy) {
      super(issueId, "Unassigned", new PublisherInfo("Issue", Issue.class, requestedBy));
      this.issueId = issueId;
    }
  }

  @Getter
  class WatchStarted extends DomainEvent {
    private final String issueId;
    private final User watcher;

    public WatchStarted(String issueId, User watcher, String requestedBy) {
      super(issueId, "WatchStarted", new PublisherInfo("Issue", Issue.class, requestedBy));
      this.issueId = issueId;
      this.watcher = watcher;
    }
  }

  @Getter
  class WatchEnded extends DomainEvent {
    private final String issueId;
    private final User watchEndedBy;

    public WatchEnded(String issueId, User watchEndedBy, String requestedBy) {
      super(issueId, "WatchStarted", new PublisherInfo("Issue", Issue.class, requestedBy));
      this.issueId = issueId;
      this.watchEndedBy = watchEndedBy;
    }
  }

  @Getter
  class Resolved extends DomainEvent {
    private final String issueId;
    private final LocalDateTime resolvedAt;

    public Resolved(String issueId, LocalDateTime resolvedAt, String resolvedBy) {
      super(issueId, "Resolved", new PublisherInfo("Issue", Issue.class, resolvedBy));
      this.issueId = issueId;
      this.resolvedAt = resolvedAt;
    }
  }

  @Getter
  class CommentAdded extends DomainEvent {
    private final String issueId;
    private final Comment comment;

    public CommentAdded(String issueId, Comment comment) {
      super(issueId, "Created", new PublisherInfo("Issue", Comment.class, comment.getUser().id()));
      this.issueId = issueId;
      this.comment = comment;
    }
  }

  @Getter
  class CommentUpdated extends DomainEvent {
    private final String issueId;
    private final Comment comment;
    // TODO: updateAt fix
    public CommentUpdated(String issueId, Comment comment) {
      super(issueId, "Updated", new PublisherInfo("Issue", Comment.class, comment.getUser().id()));
      this.issueId = issueId;
      this.comment = comment;
    }
  }
}
