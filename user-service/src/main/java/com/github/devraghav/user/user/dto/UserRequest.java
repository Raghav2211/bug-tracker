package com.github.devraghav.user.user.dto;

import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public record UserRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    com.github.devraghav.user.user.dto.AccessLevel access) {

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

  @Override
  public com.github.devraghav.user.user.dto.AccessLevel access() {
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

  private Mono<UserRequest> validateEmail(UserRequest userRequest) {
    return Mono.just(userRequest)
        .filter(UserRequest::isEmailValid)
        .switchIfEmpty(Mono.error(() -> UserException.invalidEmail(userRequest.email())));
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

  public Mono<UserRequest> validate(UserRequest userRequest) {
    return Mono.just(userRequest)
        .and(validateFirstName(userRequest))
        .and(validateLastName(userRequest))
        .and(validateEmail(userRequest))
        .thenReturn(userRequest);
  }
}
