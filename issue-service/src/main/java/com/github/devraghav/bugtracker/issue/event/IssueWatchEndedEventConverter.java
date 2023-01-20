package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.domain.issue.Unwatch;
import com.github.devraghav.data_model.event.issue.IssueUnwatched;
import com.github.devraghav.data_model.schema.issue.IssueUnwatchedSchema;
import java.time.ZoneOffset;

class IssueWatchEndedEventConverter
    implements EventConverter<IssueEvent.WatchEnded, IssueUnwatchedSchema> {

  @Override
  public IssueUnwatchedSchema convert(IssueEvent.WatchEnded event) {
    return IssueUnwatchedSchema.newBuilder()
        .setEvent(
            IssueUnwatched.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(
                    Unwatch.newBuilder()
                        .setIssueId(event.getIssueId())
                        .setRemoveWatcher(event.getWatchEndedBy())
                        .build())
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
