package com.github.devraghav.bugtracker.user.event;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.event.internal.UserDuplicatedEvent;
import com.github.devraghav.data_model.domain.user.NewUser;
import com.github.devraghav.data_model.event.user.UserDuplicated;
import com.github.devraghav.data_model.schema.user.UserDuplicatedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class UserDuplicatedEventConverter
    implements EventConverter<UserDuplicatedEvent, UserDuplicatedSchema> {

  private NewUser getUser(CreateUserRequest createUserRequest) {
    return NewUser.newBuilder()
        .setAccessLevel(createUserRequest.access().name())
        .setEmail(createUserRequest.email())
        .setFirstName(createUserRequest.firstName())
        .setLastName(createUserRequest.lastName())
        .build();
  }

  @Override
  public UserDuplicatedSchema convert(UserDuplicatedEvent event) {
    return UserDuplicatedSchema.newBuilder()
        .setEvent(
            UserDuplicated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName(event.getName())
                .setPayload(getUser(event.getDuplicateUser()))
                .setPublisher(event.getPublisher())
                .build())
        .build();
  }
}
