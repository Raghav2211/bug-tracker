package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.CreateProjectRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(Validator<CreateProjectRequest> createUserRequestValidator) {

  public Mono<CreateProjectRequest> validate(final CreateProjectRequest request) {
    return createUserRequestValidator.validate(request);
  }
}
