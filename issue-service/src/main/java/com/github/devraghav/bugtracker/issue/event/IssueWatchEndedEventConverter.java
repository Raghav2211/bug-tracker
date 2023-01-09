package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueWatchEndedEvent;
import com.github.devraghav.data_model.domain.issue.Unwatch;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.IssueUnwatched;
import com.github.devraghav.data_model.schema.issue.IssueUnwatchedSchema;
import java.time.ZoneOffset;

public class IssueWatchEndedEventConverter
    implements EventConverter<IssueWatchEndedEvent, IssueUnwatchedSchema> {
  private User getUnWatcher(com.github.devraghav.bugtracker.issue.dto.User author) {
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
  public IssueUnwatchedSchema convert(IssueWatchEndedEvent event) {
    return IssueUnwatchedSchema.newBuilder()
        .setEvent(
            IssueUnwatched.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(
                    Unwatch.newBuilder()
                        .setIssueId(event.getIssueId())
                        .setRemoveWatcher(getUnWatcher(event.getWatchEndedBy()))
                        .build())
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
