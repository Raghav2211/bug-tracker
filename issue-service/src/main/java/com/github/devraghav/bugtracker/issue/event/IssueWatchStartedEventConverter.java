package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.IssueWatchStartedEvent;
import com.github.devraghav.data_model.domain.issue.Watcher;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.IssueWatched;
import com.github.devraghav.data_model.schema.issue.IssueWatchedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class IssueWatchStartedEventConverter
    implements EventConverter<IssueWatchStartedEvent, IssueWatchedSchema> {
  private User getWatcher(com.github.devraghav.bugtracker.issue.dto.User author) {
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
  public IssueWatchedSchema convert(IssueWatchStartedEvent event) {
    return IssueWatchedSchema.newBuilder()
        .setEvent(
            IssueWatched.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Watcher.Added")
                .setPayload(
                    Watcher.newBuilder()
                        .setIssueId(event.getIssueId())
                        .setWatcher(getWatcher(event.getWatcher()))
                        .build())
                .setPublisher("Service.Issue")
                .build())
        .build();
  }
}
