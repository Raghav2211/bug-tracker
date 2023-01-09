package com.github.devraghav.bugtracker.project.event;

import com.github.devraghav.bugtracker.project.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.project.event.internal.ProjectCreatedEvent;
import com.github.devraghav.bugtracker.project.event.internal.VersionCreatedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
public final class EventConverterFactory {

  private static final VersionCreatedEventConverter VERSION_CREATED_EVENT_CONVERTER =
      new VersionCreatedEventConverter();
  private static final ProjectCreatedEventConverter PROJECT_CREATED_EVENT_CONVERTER =
      new ProjectCreatedEventConverter();

  private static final Map<Class<?>, EventConverter<?, ?>> EVENT_SOURCE_MAP = new HashMap<>();

  static {
    EVENT_SOURCE_MAP.put(ProjectCreatedEvent.class, PROJECT_CREATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(VersionCreatedEvent.class, VERSION_CREATED_EVENT_CONVERTER);
  }

  @SuppressWarnings("unchecked")
  public <S extends DomainEvent, T extends SpecificRecordBase> EventConverter<S, T> getConverter(
      Class<S> sourceClass) {
    var converter = EVENT_SOURCE_MAP.get(sourceClass);
    if (converter == null) {
      throw new NullPointerException(
          String.format("No converter found for %s", sourceClass.getName()));
    }
    return (EventConverter<S, T>) converter;
  }
}
