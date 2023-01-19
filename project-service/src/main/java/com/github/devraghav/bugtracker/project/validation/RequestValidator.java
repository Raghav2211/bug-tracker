package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {
  private final Validator<ProjectRequest.Create> createUserRequestValidator;

  public Mono<ProjectRequest.Create> validate(final ProjectRequest.Create request) {
    return createUserRequestValidator.validate(request);
  }
}
