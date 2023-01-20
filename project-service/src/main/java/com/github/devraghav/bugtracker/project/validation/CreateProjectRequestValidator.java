package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.exception.ProjectException;
import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public class CreateProjectRequestValidator implements Validator<ProjectRequest.CreateProject> {
  @Override
  public Mono<ProjectRequest.CreateProject> validate(ProjectRequest.CreateProject createProject) {
    return validateName(createProject.name())
        .and(validateDescription(createProject.description()))
        .thenReturn(createProject);
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
