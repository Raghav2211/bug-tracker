package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.domain.issue.Resolve;
import com.github.devraghav.data_model.event.issue.IssueResolved;
import com.github.devraghav.data_model.schema.issue.IssueResolvedSchema;
import java.time.ZoneOffset;

class IssueResolvedEventConverter
    implements EventConverter<IssueEvent.Resolved, IssueResolvedSchema> {

  @Override
  public IssueResolvedSchema convert(IssueEvent.Resolved event) {
    return IssueResolvedSchema.newBuilder()
        .setEvent(
            IssueResolved.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(
                    Resolve.newBuilder()
                        .setIssueId(event.getIssueId())
                        .setEndedAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                        .build())
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
