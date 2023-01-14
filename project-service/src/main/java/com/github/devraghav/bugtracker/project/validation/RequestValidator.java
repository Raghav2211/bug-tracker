package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record RequestValidator(Validator<ProjectRequest.Create> createUserRequestValidator) {

  public Mono<ProjectRequest.Create> validate(final ProjectRequest.Create request) {
    return createUserRequestValidator.validate(request);
  }
}
