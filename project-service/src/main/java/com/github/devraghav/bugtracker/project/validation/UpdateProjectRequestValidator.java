package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
class UpdateProjectRequestValidator implements Validator<ProjectRequest.UpdateProject> {
  @Override
  public Mono<ProjectRequest.UpdateProject> validate(ProjectRequest.UpdateProject updateProject) {
    return validateDescription(updateProject.description()).thenReturn(updateProject);
  }

  private Mono<Void> validateDescription(String description) {
    return Mono.just(description)
        .filter(projectDesc -> StringUtils.hasLength(projectDesc) && projectDesc.length() <= 500)
        .onErrorResume(NullPointerException.class, npe -> Mono.empty())
        .then();
  }
}
