package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.domain.issue.Unassign;
import com.github.devraghav.data_model.event.issue.IssueUnassigned;
import com.github.devraghav.data_model.schema.issue.IssueUnassignedSchema;
import java.time.ZoneOffset;

class IssueUnassignedEventConverter
    implements EventConverter<IssueEvent.Unassigned, IssueUnassignedSchema> {

  @Override
  public IssueUnassignedSchema convert(IssueEvent.Unassigned event) {
    return IssueUnassignedSchema.newBuilder()
        .setEvent(
            IssueUnassigned.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(Unassign.newBuilder().setIssueId(event.getIssueId()).build())
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
