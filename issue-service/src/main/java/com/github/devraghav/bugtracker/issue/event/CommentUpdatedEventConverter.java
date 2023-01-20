package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.dto.CommentRequestResponse;
import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.event.issue.comment.CommentUpdated;
import com.github.devraghav.data_model.schema.issue.CommentUpdatedSchema;
import java.time.ZoneOffset;

class CommentUpdatedEventConverter
    implements EventConverter<IssueEvent.CommentUpdated, CommentUpdatedSchema> {

  private com.github.devraghav.data_model.domain.issue.comment.Comment getComment(
      String issueId, CommentRequestResponse.CommentResponse comment) {
    return com.github.devraghav.data_model.domain.issue.comment.Comment.newBuilder()
        .setId(comment.id())
        .setIssueId(issueId)
        .setContent(comment.content())
        .setCommenter(comment.userId())
        .setCreatedAt(comment.createdAt().toEpochSecond(ZoneOffset.UTC))
        .build();
  }

  @Override
  public CommentUpdatedSchema convert(IssueEvent.CommentUpdated event) {
    return CommentUpdatedSchema.newBuilder()
        .setEvent(
            CommentUpdated.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getComment(event.getIssueId(), event.getComment()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
