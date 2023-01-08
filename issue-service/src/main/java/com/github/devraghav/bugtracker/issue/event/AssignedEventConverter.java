package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.AssignedEvent;
import com.github.devraghav.data_model.domain.issue.Assign;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.IssueAssigned;
import com.github.devraghav.data_model.schema.issue.IssueAssignedSchema;
import java.time.ZoneOffset;

public class AssignedEventConverter implements EventConverter<AssignedEvent, IssueAssignedSchema> {

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
  public IssueAssignedSchema convert(AssignedEvent event) {
    return IssueAssignedSchema.newBuilder()
        .setEvent(
            IssueAssigned.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
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
