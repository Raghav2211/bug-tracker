package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import com.github.devraghav.bugtracker.project.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {
  private final Validator<ProjectRequest.CreateProject> createUserRequestValidator;
  private final Validator<ProjectRequest.UpdateProject> updateProjectValidator;

  public Mono<ProjectRequest.CreateProject> validate(final ProjectRequest.CreateProject request) {
    return createUserRequestValidator.validate(request);
  }

  public Mono<ProjectRequest.UpdateProject> validate(final ProjectRequest.UpdateProject request) {
    return updateProjectValidator.validate(request);
  }
}
