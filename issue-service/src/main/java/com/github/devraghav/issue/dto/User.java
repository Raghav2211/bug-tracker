package com.github.devraghav.issue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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

  @JsonIgnore
  public boolean hasWriteAccess() {
    return this.accessLevel == AccessLevel.ADMIN || this.accessLevel == AccessLevel.WRITE;
  }
}
