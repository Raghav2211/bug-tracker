package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.validation.Validator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(Validator<UserRequest.Create> createUserRequestValidator) {

  public Mono<UserRequest.Create> validate(final UserRequest.Create request) {
    return createUserRequestValidator.validate(request);
  }
}
