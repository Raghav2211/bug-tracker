package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class UserDuplicatedEvent extends DomainEvent {
  private final CreateUserRequest duplicateUser;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("User", User.class);

  public UserDuplicatedEvent(CreateUserRequest duplicateUser) {
    super("Duplicated", publisherInfo);
    this.duplicateUser = duplicateUser;
  }
}
