package com.github.devraghav.bugtracker.user.event;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.user.event.internal.UserCreatedEvent;
import com.github.devraghav.bugtracker.user.event.internal.UserDuplicatedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
public final class EventConverterFactory {

  private static final UserDuplicatedEventConverter USER_DUPLICATED_EVENT_CONVERTER =
      new UserDuplicatedEventConverter();
  private static final UserCreatedEventConverter USER_CREATED_EVENT_CONVERTER =
      new UserCreatedEventConverter();

  private static final Map<Class<?>, EventConverter<?, ?>> EVENT_SOURCE_MAP = new HashMap<>();

  static {
    EVENT_SOURCE_MAP.put(UserDuplicatedEvent.class, USER_DUPLICATED_EVENT_CONVERTER);
    EVENT_SOURCE_MAP.put(UserCreatedEvent.class, USER_CREATED_EVENT_CONVERTER);
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
