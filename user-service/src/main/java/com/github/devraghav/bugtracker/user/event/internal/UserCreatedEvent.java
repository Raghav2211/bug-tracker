package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.User;
import lombok.Getter;

@Getter
public class UserCreatedEvent extends DomainEvent {
  private final User createdUser;

  public UserCreatedEvent(User createdUser) {
    super("User", "Created", User.class);
    this.createdUser = createdUser;
  }
}
