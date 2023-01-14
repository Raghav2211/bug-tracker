package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.issue.event.internal.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
final class EventConverterFactory {

  private static final IssueResolvedEventConverter ISSUE_RESOLVED_EVENT_CONVERTER =
      new IssueResolvedEventConverter();
  private static final IssueCreatedEventConverter ISSUE_CREATED_EVENT_CONVERTER =
      new IssueCreatedEventConverter();
  private static final IssueUpdatedEventConverter ISSUE_UPDATED_EVENT_CONVERTER =
      new IssueUpdatedEventConverter();
  private static final AssignedEventConverter ISSUE_ASSIGNED_EVENT_CONVERTER =
      new AssignedEventConverter();
  private static final IssueUnassignedEventConverter ISSUE_UNASSIGNED_EVENT_CONVERTER =
      new IssueUnassignedEventConverter();
  private static final IssueWatchStartedEventConverter ISSUE_WATCH_STARTED_EVENT_CONVERTER =
      new IssueWatchStartedEventConverter();
  private static final IssueWatchEndedEventConverter ISSUE_WATCH_ENDED_EVENT_CONVERTER =
      new IssueWatchEndedEventConverter();
  private static final CommentAddedEventConverter ISSUE_COMMENT_ADDED_EVENT_CONVERTER =
      new CommentAddedEventConverter();
  private static final CommentUpdatedEventConverter ISSUE_COMMENT_UPDATED_EVENT_CONVERTER =
      new CommentUpdatedEventConverter();

  private static final Map<Class<?>, EventConverter<?, ?>> EVENT_SOURCE_MAP = new HashMap<>();

  static {
    EVENT_SOURCE_MAP.put(IssueEvent.Created.class, ISSUE_CREATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.Updated.class, ISSUE_UPDATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.Assigned.class, ISSUE_ASSIGNED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.Unassigned.class, ISSUE_UNASSIGNED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.WatchStarted.class, ISSUE_WATCH_STARTED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.WatchEnded.class, ISSUE_WATCH_ENDED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.CommentAdded.class, ISSUE_COMMENT_ADDED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.CommentUpdated.class, ISSUE_COMMENT_UPDATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueEvent.Resolved.class, ISSUE_RESOLVED_EVENT_CONVERTER);
  }

  @SuppressWarnings("unchecked")
  public <T extends DomainEvent, E extends SpecificRecordBase> EventConverter<T, E> getConverter(
      Class<T> sourceClass) {
    var converter = EVENT_SOURCE_MAP.get(sourceClass);
    if (converter == null) {
      throw new NullPointerException(
          String.format("No converter found for %s", sourceClass.getName()));
    }
    return (EventConverter<T, E>) converter;
  }
}
