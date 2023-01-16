package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.user.dto.User;
import lombok.Getter;

public interface UserEvent {

  @Getter
  class Created extends DomainEvent {
    private final User createdUser;

    public Created(User createdUser) {
      super(createdUser.id(), "Created", new PublisherInfo("User", User.class, "SYSTEM"));
      this.createdUser = createdUser;
    }
  }
}
