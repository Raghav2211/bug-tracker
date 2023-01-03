package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.data_model.event.user.UserCreated;
import com.github.devraghav.data_model.schema.user.UserCreatedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Function;

public class UserCreatedDomainIntegrationEventConverter
    implements DomainIntegrationEventConverter<User, UserCreatedSchema> {

  private com.github.devraghav.data_model.domain.user.User getUser(User user) {
    return com.github.devraghav.data_model.domain.user.User.newBuilder()
        .setId(user.id())
        .setAccessLevel(user.access().name())
        .setEmail(user.email())
        .setEnabled(user.enabled())
        .setFirstName(user.firstName())
        .setLastName(user.lastName())
        .build();
  }

  @Override
  public Function<User, UserCreatedSchema> domainToIntegrationFunc() {
    return user ->
        UserCreatedSchema.newBuilder()
            .setEvent(
                UserCreated.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                    .setName("User.User.Created")
                    .setPayload(getUser(user))
                    .setPublisher("Service.User")
                    .build())
            .build();
  }
}
