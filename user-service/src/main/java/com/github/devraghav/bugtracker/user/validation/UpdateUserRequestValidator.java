package com.github.devraghav.bugtracker.user.validation;

import com.github.devraghav.bugtracker.user.exception.UserException;
import com.github.devraghav.bugtracker.user.request.UserRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
class UpdateUserRequestValidator implements Validator<UserRequest.UpdateUser> {

  @Override
  public Mono<UserRequest.UpdateUser> validate(UserRequest.UpdateUser updateUser) {
    return validateFirstName(updateUser.firstName())
        .and(validateLastName(updateUser.lastName()))
        .thenReturn(updateUser);
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
