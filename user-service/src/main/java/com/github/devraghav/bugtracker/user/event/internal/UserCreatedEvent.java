package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.dto.User;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class UserCreatedEvent extends DomainEvent {
  private final User createdUser;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("User", User.class);

  public UserCreatedEvent(User createdUser) {
    super("Created", publisherInfo);
    this.createdUser = createdUser;
  }
}
