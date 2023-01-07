package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.event.internal.CommentUpdatedEvent;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.comment.CommentUpdated;
import com.github.devraghav.data_model.schema.issue.CommentUpdatedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class IssueCommentUpdatedEventConverter
    implements EventConverter<CommentUpdatedEvent, CommentUpdatedSchema> {

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
  public CommentUpdatedSchema convert(CommentUpdatedEvent event) {
    return CommentUpdatedSchema.newBuilder()
        .setEvent(
            CommentUpdated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getComment(event.getIssueId(), event.getComment()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
