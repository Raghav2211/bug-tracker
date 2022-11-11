package com.github.devraghav.bugtracker.user.dto;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Data
public class UserRequest {
  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private com.github.devraghav.bugtracker.user.dto.AccessLevel access =
      com.github.devraghav.bugtracker.user.dto.AccessLevel.READ;

  private boolean hasFirstName() {
    return StringUtils.hasLength(this.firstName);
  }

  private boolean hasLastName() {
    return StringUtils.hasLength(this.lastName);
  }

  private boolean isEmailValid() {
    Pattern pattern = Pattern.compile(EMAIL_REGEX);
    return pattern.matcher(this.email).matches();
  }

  public Mono<UserRequest> validate(UserRequest userRequest) {
    return Mono.just(userRequest)
        .and(validateFirstName(userRequest))
        .and(validateLastName(userRequest))
        .and(validateEmail(userRequest))
        .thenReturn(userRequest);
  }

  private Mono<UserRequest> validateEmail(UserRequest userRequest) {
    return Mono.just(userRequest)
        .filter(UserRequest::isEmailValid)
        .switchIfEmpty(Mono.error(() -> UserException.invalidEmail(userRequest.getEmail())));
  }

  private Mono<UserRequest> validateLastName(UserRequest userRequest) {
    return Mono.just(userRequest)
        .filter(UserRequest::hasLastName)
        .switchIfEmpty(Mono.error(UserException::nullLastName));
  }

  private Mono<UserRequest> validateFirstName(UserRequest userRequest) {
    return Mono.just(userRequest)
        .filter(UserRequest::hasFirstName)
        .switchIfEmpty(Mono.error(UserException::nullFirstName));
  }
}
