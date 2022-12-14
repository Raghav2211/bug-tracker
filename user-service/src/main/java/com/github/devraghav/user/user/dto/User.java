package com.github.devraghav.user.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

  private String id;
  private AccessLevel access;
  private String firstName;
  private String lastName;
  private String email;
  private Boolean enabled;

  @JsonIgnore
  public boolean isWriteAccess() {
    return this.access == AccessLevel.ADMIN || this.access == AccessLevel.WRITE;
  }
}
