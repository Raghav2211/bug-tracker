package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {
  private final Validator<ProjectRequest.CreateProject> createUserRequestValidator;

  public Mono<ProjectRequest.CreateProject> validate(final ProjectRequest.CreateProject request) {
    return createUserRequestValidator.validate(request);
  }
}
