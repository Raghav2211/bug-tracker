package com.github.devraghav.bugtracker.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

  private String id;
  private AccessLevel accessLevel;
  private String firstName;
  private String lastName;
  private String email;
  private Boolean enabled;

  public User(UserEntity user) {
    this.id = user.getId();
    this.accessLevel = AccessLevel.fromValue(user.getAccess());
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.enabled = user.getEnabled();
  }

  @JsonIgnore
  public boolean isWriteAccess() {
    return this.accessLevel == AccessLevel.ADMIN || this.accessLevel == AccessLevel.WRITE;
  }
}
