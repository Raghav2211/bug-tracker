package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.data_model.schema.user.UserCreatedSchema;
import com.github.devraghav.data_model.schema.user.UserDuplicatedSchema;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

@Component
public final class EventConverterFactory {

  private static final UserDuplicatedEventConverter USER_DUPLICATED_EVENT_CONVERTER =
      new UserDuplicatedEventConverter();
  private static final UserCreatedEventConverter USER_CREATED_EVENT_CONVERTER =
      new UserCreatedEventConverter();

  private static final Map<Class<? extends DomainEvent>, Event> EVENT_SOURCE_MAP =
      Arrays.stream(Event.values())
          .sequential()
          .collect(Collectors.toMap(Event::getSourceClazz, Function.identity()));

  @Getter
  @SuppressWarnings("unchecked")
  private enum Event {
    USER_DUPLICATED_EVENT(UserDuplicatedEvent.class) {
      @Override
      EventConverter<UserDuplicatedEvent, UserDuplicatedSchema> getConverter() {
        return USER_DUPLICATED_EVENT_CONVERTER;
      }
    },
    USER_CREATED_EVENT(UserCreatedEvent.class) {
      @Override
      EventConverter<UserCreatedEvent, UserCreatedSchema> getConverter() {
        return USER_CREATED_EVENT_CONVERTER;
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
