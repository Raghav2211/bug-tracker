package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.data_model.event.user.UserCreated;
import com.github.devraghav.data_model.schema.user.UserCreatedSchema;
import java.time.ZoneOffset;

public class UserCreatedEventConverter
    implements EventConverter<UserCreatedEvent, UserCreatedSchema> {

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
  public UserCreatedSchema convert(UserCreatedEvent event) {
    return UserCreatedSchema.newBuilder()
        .setEvent(
            UserCreated.newBuilder()
                .setId(event.getId().toString())
                .setCreateAt(event.getLogTime().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getUser(event.getCreatedUser()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
