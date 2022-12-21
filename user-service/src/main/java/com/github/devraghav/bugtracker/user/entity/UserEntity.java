package com.github.devraghav.bugtracker.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
@Data
@NoArgsConstructor
public class UserEntity {
  @Id private String id;
  private String password;
  private Integer access;
  private String firstName;
  private String lastName;
  private String email;
  private Boolean enabled;
}
