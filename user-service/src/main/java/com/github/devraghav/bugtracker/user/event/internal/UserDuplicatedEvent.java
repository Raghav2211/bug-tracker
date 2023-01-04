package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
import lombok.Getter;

@Getter
public class UserDuplicatedEvent extends DomainEvent {
  private final CreateUserRequest duplicateUser;

  public UserDuplicatedEvent(CreateUserRequest duplicateUser) {
    super("User", "Duplicated", User.class);
    this.duplicateUser = duplicateUser;
  }
}
