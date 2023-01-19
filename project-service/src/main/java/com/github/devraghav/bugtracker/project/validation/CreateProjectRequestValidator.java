package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.RequestResponse;
import com.github.devraghav.bugtracker.project.exception.ProjectException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public class CreateProjectRequestValidator
    implements Validator<RequestResponse.CreateProjectRequest> {
  @Override
  public Mono<RequestResponse.CreateProjectRequest> validate(
      RequestResponse.CreateProjectRequest createProjectRequest) {
    return validateName(createProjectRequest.name())
        .and(validateDescription(createProjectRequest.description()))
        .thenReturn(createProjectRequest);
  }

  private Mono<Void> validateName(String name) {
    return Mono.justOrEmpty(name)
        .filter(
            projectName -> StringUtils.hasLength(projectName) && projectName.matches("^[a-zA-Z]*$"))
        .switchIfEmpty(Mono.error(() -> ProjectException.invalidName(name)))
        .then();
  }

  private Mono<Void> validateDescription(String description) {
    return Mono.justOrEmpty(description)
        .filter(projectDesc -> StringUtils.hasLength(projectDesc) && projectDesc.length() <= 200)
        .switchIfEmpty(Mono.error(() -> ProjectException.invalidDescription(description)))
        .then();
  }
}
