package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.data_model.schema.issue.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
public final class EventConverterFactory {

  private static final IssueResolvedEventConverter ISSUE_RESOLVED_EVENT_CONVERTER =
      new IssueResolvedEventConverter();
  private static final IssueCreatedEventConverter ISSUE_CREATED_EVENT_CONVERTER =
      new IssueCreatedEventConverter();
  private static final IssueUpdatedEventConverter ISSUE_UPDATED_EVENT_CONVERTER =
      new IssueUpdatedEventConverter();
  private static final IssueAssignedEventConverter ISSUE_ASSIGNED_EVENT_CONVERTER =
      new IssueAssignedEventConverter();
  private static final IssueUnassignedEventConverter ISSUE_UNASSIGNED_EVENT_CONVERTER =
      new IssueUnassignedEventConverter();
  private static final IssueWatchStartedEventConverter ISSUE_WATCH_STARTED_EVENT_CONVERTER =
      new IssueWatchStartedEventConverter();
  private static final IssueWatchEndedEventConverter ISSUE_WATCH_ENDED_EVENT_CONVERTER =
      new IssueWatchEndedEventConverter();
  private static final IssueCommentAddedEventConverter ISSUE_COMMENT_ADDED_EVENT_CONVERTER =
      new IssueCommentAddedEventConverter();
  private static final IssueCommentUpdatedEventConverter ISSUE_COMMENT_UPDATED_EVENT_CONVERTER =
      new IssueCommentUpdatedEventConverter();

  private static final Map<Class<?>, EventConverter<?, ?>> EVENT_SOURCE_MAP = new HashMap<>();

  static {
    EVENT_SOURCE_MAP.put(IssueCreatedEvent.class, ISSUE_CREATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueUpdatedEvent.class, ISSUE_UPDATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueAssignedEvent.class, ISSUE_ASSIGNED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueUnassignedEvent.class, ISSUE_UNASSIGNED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueWatchStartedEvent.class, ISSUE_WATCH_STARTED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueWatchEndedEvent.class, ISSUE_WATCH_ENDED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueCommentAddedEvent.class, ISSUE_COMMENT_ADDED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueCommentUpdatedEvent.class, ISSUE_COMMENT_UPDATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(IssueResolvedEvent.class, ISSUE_RESOLVED_EVENT_CONVERTER);
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
