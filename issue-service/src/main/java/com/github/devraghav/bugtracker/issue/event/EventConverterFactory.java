package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.*;
import com.github.devraghav.data_model.schema.issue.*;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
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

  private static final Map<Class<? extends DomainEvent>, Event> EVENT_SOURCE_MAP =
      Arrays.stream(Event.values())
          .sequential()
          .collect(Collectors.toMap(Event::getSourceClazz, Function.identity()));

  @Getter
  @SuppressWarnings("unchecked")
  private enum Event {
    ISSUE_CREATED_EVENT(IssueCreatedEvent.class) {
      @Override
      EventConverter<IssueCreatedEvent, IssueCreatedSchema> getConverter() {
        return ISSUE_CREATED_EVENT_CONVERTER;
      }
    },
    ISSUE_UPDATED_EVENT(IssueUpdatedEvent.class) {
      @Override
      EventConverter<IssueUpdatedEvent, IssueUpdatedSchema> getConverter() {
        return ISSUE_UPDATED_EVENT_CONVERTER;
      }
    },
    ISSUE_ASSIGNED_EVENT(IssueAssignedEvent.class) {
      @Override
      EventConverter<IssueAssignedEvent, IssueAssignedSchema> getConverter() {
        return ISSUE_ASSIGNED_EVENT_CONVERTER;
      }
    },
    ISSUE_RESOLVED_EVENT(IssueResolvedEvent.class) {
      @Override
      EventConverter<IssueResolvedEvent, IssueResolvedSchema> getConverter() {
        return ISSUE_RESOLVED_EVENT_CONVERTER;
      }
    },
    ISSUE_UNASSIGNED_EVENT(IssueUnassignedEvent.class) {
      @Override
      EventConverter<IssueUnassignedEvent, IssueUnassignedSchema> getConverter() {
        return ISSUE_UNASSIGNED_EVENT_CONVERTER;
      }
    },
    ISSUE_WATCH_STARTED_EVENT(IssueWatchStartedEvent.class) {
      @Override
      EventConverter<IssueWatchStartedEvent, IssueWatchedSchema> getConverter() {
        return ISSUE_WATCH_STARTED_EVENT_CONVERTER;
      }
    },
    ISSUE_WATCH_ENDED_EVENT(IssueWatchEndedEvent.class) {
      @Override
      EventConverter<IssueWatchEndedEvent, IssueUnwatchedSchema> getConverter() {
        return ISSUE_WATCH_ENDED_EVENT_CONVERTER;
      }
    },
    ISSUE_COMMENT_CREATED_EVENT(IssueCommentAddedEvent.class) {
      @Override
      EventConverter<IssueCommentAddedEvent, CommentAddedSchema> getConverter() {
        return ISSUE_COMMENT_ADDED_EVENT_CONVERTER;
      }
    },
    ISSUE_COMMENT_UPDATED_EVENT(IssueCommentUpdatedEvent.class) {
      @Override
      EventConverter<IssueCommentUpdatedEvent, CommentUpdatedSchema> getConverter() {
        return ISSUE_COMMENT_UPDATED_EVENT_CONVERTER;
      }
    };

    abstract <T extends DomainEvent, R extends SpecificRecordBase>
        EventConverter<T, R> getConverter();

    final Class<? extends DomainEvent> sourceClazz;

    Event(Class<? extends DomainEvent> sourceClazz) {
      this.sourceClazz = sourceClazz;
    }
  }

  public <T extends DomainEvent, E extends SpecificRecordBase> EventConverter<T, E> getConverter(
      Class<T> sourceClass) {
    var event = EVENT_SOURCE_MAP.get(sourceClass);
    if (event == null) {
      throw new NullPointerException(
          String.format("No converter found for %s", sourceClass.getName()));
    }
    return event.getConverter();
  }
}
