package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.domain.issue.Assign;
import com.github.devraghav.data_model.event.issue.IssueAssigned;
import com.github.devraghav.data_model.schema.issue.IssueAssignedSchema;
import java.time.ZoneOffset;

class AssignedEventConverter implements EventConverter<IssueEvent.Assigned, IssueAssignedSchema> {

  @Override
  public IssueAssignedSchema convert(IssueEvent.Assigned event) {
    return IssueAssignedSchema.newBuilder()
        .setEvent(
            IssueAssigned.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(
                    Assign.newBuilder()
                        .setIssueId(event.getIssueId())
                        .setAssignee(event.getAssignee())
                        .build())
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
