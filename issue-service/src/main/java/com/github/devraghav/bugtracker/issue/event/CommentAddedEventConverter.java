package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.dto.CommentRequestResponse;
import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.event.issue.comment.CommentAdded;
import com.github.devraghav.data_model.schema.issue.CommentAddedSchema;
import java.time.ZoneOffset;

class CommentAddedEventConverter
    implements EventConverter<IssueEvent.CommentAdded, CommentAddedSchema> {

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
  public CommentAddedSchema convert(IssueEvent.CommentAdded event) {
    return CommentAddedSchema.newBuilder()
        .setEvent(
            CommentAdded.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getComment(event.getIssueId(), event.getComment()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
