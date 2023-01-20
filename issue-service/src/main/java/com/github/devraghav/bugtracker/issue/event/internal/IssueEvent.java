package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.issue.dto.CommentRequestResponse;
import com.github.devraghav.bugtracker.issue.dto.IssueRequestResponse;
import java.time.LocalDateTime;
import lombok.Getter;

public interface IssueEvent {

  @Getter
  class Created extends DomainEvent {
    private final IssueRequestResponse.IssueResponse createdIssue;

    public Created(IssueRequestResponse.IssueResponse createdIssue) {
      super(
          createdIssue.id(),
          "Created",
          new PublisherInfo(
              "Issue", IssueRequestResponse.IssueResponse.class, createdIssue.reporter()));
      this.createdIssue = createdIssue;
    }
  }

  @Getter
  class Updated extends DomainEvent {
    private final IssueRequestResponse.IssueResponse updatedIssue;

    public Updated(String updateBy, IssueRequestResponse.IssueResponse updatedIssue) {
      super(
          updatedIssue.id(),
          "Updated",
          new PublisherInfo("Issue", IssueRequestResponse.IssueResponse.class, updateBy));
      this.updatedIssue = updatedIssue;
    }
  }

  @Getter
  class Assigned extends DomainEvent {
    private final String issueId;
    private final String assignee;

    public Assigned(String issueId, String assignee, String requestedBy) {
      super(
          issueId,
          "Assigned",
          new PublisherInfo("Issue", IssueRequestResponse.IssueResponse.class, requestedBy));
      this.issueId = issueId;
      this.assignee = assignee;
    }
  }

  @Getter
  class Unassigned extends DomainEvent {
    private final String issueId;

    public Unassigned(String issueId, String requestedBy) {
      super(
          issueId,
          "Unassigned",
          new PublisherInfo("Issue", IssueRequestResponse.IssueResponse.class, requestedBy));
      this.issueId = issueId;
    }
  }

  @Getter
  class WatchStarted extends DomainEvent {
    private final String issueId;
    private final String watcher;

    public WatchStarted(String issueId, String watcher, String requestedBy) {
      super(
          issueId,
          "WatchStarted",
          new PublisherInfo("Issue", IssueRequestResponse.IssueResponse.class, requestedBy));
      this.issueId = issueId;
      this.watcher = watcher;
    }
  }

  @Getter
  class WatchEnded extends DomainEvent {
    private final String issueId;
    private final String watchEndedBy;

    public WatchEnded(String issueId, String watchEndedBy, String requestedBy) {
      super(
          issueId,
          "WatchStarted",
          new PublisherInfo("Issue", IssueRequestResponse.IssueResponse.class, requestedBy));
      this.issueId = issueId;
      this.watchEndedBy = watchEndedBy;
    }
  }

  @Getter
  class Resolved extends DomainEvent {
    private final String issueId;
    private final LocalDateTime resolvedAt;

    public Resolved(String issueId, LocalDateTime resolvedAt, String resolvedBy) {
      super(
          issueId,
          "Resolved",
          new PublisherInfo("Issue", IssueRequestResponse.IssueResponse.class, resolvedBy));
      this.issueId = issueId;
      this.resolvedAt = resolvedAt;
    }
  }

  @Getter
  class CommentAdded extends DomainEvent {
    private final String issueId;
    private final CommentRequestResponse.CommentResponse comment;

    public CommentAdded(String issueId, CommentRequestResponse.CommentResponse comment) {
      super(
          issueId,
          "Created",
          new PublisherInfo(
              "Issue", CommentRequestResponse.CommentResponse.class, comment.userId()));
      this.issueId = issueId;
      this.comment = comment;
    }
  }

  @Getter
  class CommentUpdated extends DomainEvent {
    private final String issueId;
    private final CommentRequestResponse.CommentResponse comment;
    // TODO: updateAt fix
    public CommentUpdated(String issueId, CommentRequestResponse.CommentResponse comment) {
      super(
          issueId,
          "Updated",
          new PublisherInfo(
              "Issue", CommentRequestResponse.CommentResponse.class, comment.userId()));
      this.issueId = issueId;
      this.comment = comment;
    }
  }
}
