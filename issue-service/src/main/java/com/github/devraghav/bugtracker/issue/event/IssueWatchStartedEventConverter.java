package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import com.github.devraghav.data_model.domain.issue.Watcher;
import com.github.devraghav.data_model.event.issue.IssueWatched;
import com.github.devraghav.data_model.schema.issue.IssueWatchedSchema;
import java.time.ZoneOffset;

class IssueWatchStartedEventConverter
    implements EventConverter<IssueEvent.WatchStarted, IssueWatchedSchema> {

  @Override
  public IssueWatchedSchema convert(IssueEvent.WatchStarted event) {
    return IssueWatchedSchema.newBuilder()
        .setEvent(
            IssueWatched.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(
                    Watcher.newBuilder()
                        .setIssueId(event.getIssueId())
                        .setWatcher(event.getWatcher())
                        .build())
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
