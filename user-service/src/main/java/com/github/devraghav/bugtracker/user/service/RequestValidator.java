package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.request.UserRequest;
import com.github.devraghav.bugtracker.user.validation.Validator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(
    Validator<UserRequest.CreateUser> createUserValidator,
    Validator<UserRequest.UpdateUser> updateUserValidator) {

  public Mono<UserRequest.CreateUser> validate(final UserRequest.CreateUser request) {
    return createUserValidator.validate(request);
  }

  public Mono<UserRequest.UpdateUser> validate(final UserRequest.UpdateUser request) {
    return updateUserValidator.validate(request);
  }
}
