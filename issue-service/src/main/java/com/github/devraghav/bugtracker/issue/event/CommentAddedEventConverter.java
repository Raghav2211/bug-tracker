package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.event.internal.CommentAddedEvent;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.comment.CommentAdded;
import com.github.devraghav.data_model.schema.issue.CommentAddedSchema;
import java.time.ZoneOffset;

public class CommentAddedEventConverter
    implements EventConverter<CommentAddedEvent, CommentAddedSchema> {

  private User getUser(com.github.devraghav.bugtracker.issue.dto.User author) {
    return User.newBuilder()
        .setId(author.id())
        .setAccessLevel(author.access().name())
        .setEmail(author.email())
        .setEnabled(author.enabled())
        .setFirstName(author.firstName())
        .setLastName(author.lastName())
        .build();
  }

  private com.github.devraghav.data_model.domain.issue.comment.Comment getComment(
      String issueId, Comment comment) {
    return com.github.devraghav.data_model.domain.issue.comment.Comment.newBuilder()
        .setId(comment.getId())
        .setIssueId(issueId)
        .setContent(comment.getContent())
        .setCommenter(getUser(comment.getUser()))
        .setCreatedAt(comment.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
        .build();
  }

  @Override
  public CommentAddedSchema convert(CommentAddedEvent event) {
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
