package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueAssignedEvent;
import com.github.devraghav.data_model.domain.issue.Assign;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.IssueAssigned;
import com.github.devraghav.data_model.schema.issue.IssueAssignedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class IssueAssignedEventConverter
    implements EventConverter<IssueAssignedEvent, IssueAssignedSchema> {

  private User getAssignee(com.github.devraghav.bugtracker.issue.dto.User author) {
    return User.newBuilder()
        .setId(author.id())
        .setAccessLevel(author.access().name())
        .setEmail(author.email())
        .setEnabled(author.enabled())
        .setFirstName(author.firstName())
        .setLastName(author.lastName())
        .build();
  }

  @Override
  public IssueAssignedSchema convert(IssueAssignedEvent event) {
    return IssueAssignedSchema.newBuilder()
        .setEvent(
            IssueAssigned.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(
                    Assign.newBuilder()
                        .setIssueId(event.getIssueId())
                        .setAssignee(getAssignee(event.getAssignee()))
                        .build())
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
