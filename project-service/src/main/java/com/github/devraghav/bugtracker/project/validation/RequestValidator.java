package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.CreateProjectRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(
    ValidationStrategy<CreateProjectRequest> createUserRequestValidationStrategy) {

  public Mono<CreateProjectRequest> validate(final CreateProjectRequest request) {
    return createUserRequestValidationStrategy.validate(request);
  }
}
