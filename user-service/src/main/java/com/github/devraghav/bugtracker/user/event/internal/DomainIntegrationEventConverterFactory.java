package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
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
public final class DomainIntegrationEventConverterFactory {

  private static final UserDuplicatedDomainIntegrationEventConverter
      USER_DUPLICATED_EVENT_CONVERTER = new UserDuplicatedDomainIntegrationEventConverter();
  private static final UserCreatedDomainIntegrationEventConverter USER_CREATED_EVENT_CONVERTER =
      new UserCreatedDomainIntegrationEventConverter();

  private static final Map<Class<?>, Event> EVENT_SOURCE_MAP =
      Arrays.stream(Event.values())
          .sequential()
          .collect(Collectors.toMap(Event::getSourceClazz, Function.identity()));

  @Getter
  @SuppressWarnings("unchecked")
  private enum Event {
    USER_DUPLICATED_EVENT(CreateUserRequest.class) {
      @Override
      DomainIntegrationEventConverter<CreateUserRequest, UserDuplicatedSchema> getConverter() {
        return USER_DUPLICATED_EVENT_CONVERTER;
      }
    },
    USER_CREATED_EVENT(User.class) {
      @Override
      DomainIntegrationEventConverter<User, UserCreatedSchema> getConverter() {
        return USER_CREATED_EVENT_CONVERTER;
      }
    };

    abstract <T, R extends SpecificRecordBase> DomainIntegrationEventConverter<T, R> getConverter();

    final Class<?> sourceClazz;

    Event(Class<?> sourceClazz) {
      this.sourceClazz = sourceClazz;
    }
  }

  public <T, E extends SpecificRecordBase> DomainIntegrationEventConverter<T, E> getConverter(
      Class<T> sourceClass) {
    var event = EVENT_SOURCE_MAP.get(sourceClass);
    if (event == null) {
      throw new NullPointerException(
          String.format("No converter found for %s", sourceClass.getName()));
    }
    return event.getConverter();
  }
}
