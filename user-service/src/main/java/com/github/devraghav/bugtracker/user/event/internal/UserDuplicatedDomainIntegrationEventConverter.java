package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.data_model.domain.user.NewUser;
import com.github.devraghav.data_model.event.user.UserDuplicated;
import com.github.devraghav.data_model.schema.user.UserDuplicatedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Function;

public class UserDuplicatedDomainIntegrationEventConverter
    implements DomainIntegrationEventConverter<CreateUserRequest, UserDuplicatedSchema> {
  public UserDuplicatedDomainIntegrationEventConverter() {
    System.out.println("Call");
  }

  private NewUser getUser(CreateUserRequest createUserRequest) {
    return NewUser.newBuilder()
        .setAccessLevel(createUserRequest.access().name())
        .setEmail(createUserRequest.email())
        .setFirstName(createUserRequest.firstName())
        .setLastName(createUserRequest.lastName())
        .build();
  }

  @Override
  public Function<CreateUserRequest, UserDuplicatedSchema> domainToIntegrationFunc() {
    return createUserRequest ->
        UserDuplicatedSchema.newBuilder()
            .setEvent(
                UserDuplicated.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                    .setName("User.User.Duplicated")
                    .setPayload(getUser(createUserRequest))
                    .setPublisher("Service.User")
                    .build())
            .build();
  }
}
