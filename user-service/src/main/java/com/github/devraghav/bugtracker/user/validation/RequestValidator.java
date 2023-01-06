package com.github.devraghav.bugtracker.user.validation;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(Validator<CreateUserRequest> createUserRequestValidator) {

  public Mono<CreateUserRequest> validate(final CreateUserRequest request) {
    return createUserRequestValidator.validate(request);
  }
}
