package com.github.devraghav.bugtracker.user.validation;

import com.github.devraghav.bugtracker.user.exception.UserException;
import com.github.devraghav.bugtracker.user.request.UserRequest;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
class CreateUserRequestValidator implements Validator<UserRequest.CreateUser> {

  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

  @Override
  public Mono<UserRequest.CreateUser> validate(UserRequest.CreateUser createUserUserRequest) {
    return validateFirstName(createUserUserRequest.firstName())
        .and(validateLastName(createUserUserRequest.lastName()))
        .and(validateEmail(createUserUserRequest.email()))
        .thenReturn(createUserUserRequest);
  }

  private Mono<Void> validateEmail(String email) {
    return Mono.justOrEmpty(email)
        .filter(nonNullEmail -> Pattern.compile(EMAIL_REGEX).matcher(nonNullEmail).matches())
        .switchIfEmpty(Mono.error(UserException.invalidEmail(email)))
        .then();
  }

  private Mono<Void> validateLastName(String lastName) {
    return Mono.justOrEmpty(lastName)
        .filter(StringUtils::hasLength)
        .switchIfEmpty(Mono.error(UserException.nullLastName()))
        .then();
  }

  private Mono<Void> validateFirstName(String firstName) {
    return Mono.justOrEmpty(firstName)
        .filter(StringUtils::hasLength)
        .switchIfEmpty(Mono.error(UserException.nullFirstName()))
        .then();
  }
}
