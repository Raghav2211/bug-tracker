package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.issue.dto.IssueComment;
import com.github.devraghav.data_model.domain.issue.comment.Comment;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.comment.CommentAdded;
import com.github.devraghav.data_model.schema.issue.CommentAddedSchema;
import java.time.ZoneOffset;

public class IssueCommentAddedEventConverter
    implements EventConverter<IssueCommentAddedEvent, CommentAddedSchema> {

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

  private Comment getComment(String issueId, IssueComment issueComment) {
    return Comment.newBuilder()
        .setId(issueComment.getId())
        .setIssueId(issueId)
        .setContent(issueComment.getContent())
        .setCommenter(getUser(issueComment.getUser()))
        .setCreatedAt(issueComment.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
        .build();
  }

  @Override
  public CommentAddedSchema convert(IssueCommentAddedEvent event) {
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
