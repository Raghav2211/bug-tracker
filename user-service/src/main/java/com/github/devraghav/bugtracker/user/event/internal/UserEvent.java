package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.user.response.UserResponse;
import lombok.Getter;

public interface UserEvent {

  @Getter
  class Created extends DomainEvent {
    private final UserResponse.User createdUser;

    public Created(UserResponse.User createdUser) {
      super(
          createdUser.id(),
          "Created",
          new PublisherInfo("User", UserResponse.User.class, createdUser.id()));
      this.createdUser = createdUser;
    }
  }

  @Getter
  class Updated extends DomainEvent {
    private final UserResponse.User createdUser;

    public Updated(UserResponse.User createdUser) {
      super(
          createdUser.id(),
          "Updated",
          new PublisherInfo("User", UserResponse.User.class, createdUser.id()));
      this.createdUser = createdUser;
    }
  }
}
