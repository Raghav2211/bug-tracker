package com.github.devraghav.bugtracker.user.dto;

import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public record CreateUserRequest(
    String firstName, String lastName, String email, String password, AccessLevel access) {

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

  @Override
  public AccessLevel access() {
    return Objects.isNull(access) ? AccessLevel.READ : access;
  }

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

  private Mono<CreateUserRequest> validateEmail(Mono<CreateUserRequest> createUserRequestMono) {
    return createUserRequestMono
        .filter(CreateUserRequest::isEmailValid)
        .switchIfEmpty(Mono.error(() -> UserException.invalidEmail(this.email())));
  }

  private Mono<CreateUserRequest> validateLastName(Mono<CreateUserRequest> createUserRequestMono) {
    return createUserRequestMono
        .filter(CreateUserRequest::hasLastName)
        .switchIfEmpty(Mono.error(UserException::nullLastName));
  }

  private Mono<CreateUserRequest> validateFirstName(Mono<CreateUserRequest> createUserRequestMono) {
    return createUserRequestMono
        .filter(CreateUserRequest::hasFirstName)
        .switchIfEmpty(Mono.error(UserException::nullFirstName));
  }

  public Mono<CreateUserRequest> validate() {
    var userRequestMono = Mono.just(this);
    return userRequestMono
        .and(validateFirstName(userRequestMono))
        .and(validateLastName(userRequestMono))
        .and(validateEmail(userRequestMono))
        .thenReturn(this);
  }
}
