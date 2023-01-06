package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueResolvedEvent;
import com.github.devraghav.data_model.domain.issue.Resolve;
import com.github.devraghav.data_model.event.issue.IssueResolved;
import com.github.devraghav.data_model.schema.issue.IssueResolvedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class IssueResolvedEventConverter
    implements EventConverter<IssueResolvedEvent, IssueResolvedSchema> {

  @Override
  public IssueResolvedSchema convert(IssueResolvedEvent event) {
    return IssueResolvedSchema.newBuilder()
        .setEvent(
            IssueResolved.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
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
