package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {
  private final Validator<ProjectRequest.CreateProjectRequest> createUserRequestValidator;

  public Mono<ProjectRequest.CreateProjectRequest> validate(
      final ProjectRequest.CreateProjectRequest request) {
    return createUserRequestValidator.validate(request);
  }
}
