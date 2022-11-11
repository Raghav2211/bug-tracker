package com.github.devraghav.bugtracker.user.entity;

import com.github.devraghav.bugtracker.user.dto.UserRequest;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserEntity {
  private String id;
  private String password;
  private Integer access;
  private String firstName;
  private String lastName;
  private String email;
  private Boolean enabled;

  public UserEntity(UserRequest userRequest) {
    this.id = UUID.randomUUID().toString();
    this.password = userRequest.getPassword();
    this.access = userRequest.getAccess().getValue();
    this.firstName = userRequest.getFirstName();
    this.lastName = userRequest.getLastName();
    this.email = userRequest.getEmail();
    this.enabled = true;
  }
}
