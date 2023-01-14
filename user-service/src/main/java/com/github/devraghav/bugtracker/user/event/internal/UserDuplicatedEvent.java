package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class UserDuplicatedEvent extends DomainEvent {
  private final UserRequest.Create duplicateUser;

  @Getter(AccessLevel.NONE)
  private static final PublisherInfo publisherInfo = new PublisherInfo("User", User.class);

  public UserDuplicatedEvent(UserRequest.Create duplicateUser) {
    super("Duplicated", publisherInfo);
    this.duplicateUser = duplicateUser;
  }
}
