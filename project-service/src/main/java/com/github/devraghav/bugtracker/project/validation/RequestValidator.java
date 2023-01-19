package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.RequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {
  private final Validator<RequestResponse.CreateProjectRequest> createUserRequestValidator;

  public Mono<RequestResponse.CreateProjectRequest> validate(
      final RequestResponse.CreateProjectRequest request) {
    return createUserRequestValidator.validate(request);
  }
}
