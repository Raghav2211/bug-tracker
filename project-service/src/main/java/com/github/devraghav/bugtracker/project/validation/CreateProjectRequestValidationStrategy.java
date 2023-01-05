package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.CreateProjectRequest;
import com.github.devraghav.bugtracker.project.dto.ProjectException;
import com.github.devraghav.bugtracker.project.dto.User;
import com.github.devraghav.bugtracker.project.dto.UserClientException;
import com.github.devraghav.bugtracker.project.service.UserReactiveClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
public record CreateProjectRequestValidationStrategy(UserReactiveClient userReactiveClient)
    implements ValidationStrategy<CreateProjectRequest> {
  @Override
  public Mono<CreateProjectRequest> validate(CreateProjectRequest createProjectRequest) {
    return validateName(createProjectRequest.name())
        .and(validateDescription(createProjectRequest.description()))
        .and(validateAuthor(createProjectRequest.author()))
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

  private Mono<Void> validateAuthor(String author) {
    return Mono.justOrEmpty(author)
        .filter(StringUtils::hasLength)
        .switchIfEmpty(Mono.error(ProjectException::nullAuthor))
        .flatMap(this::fetchAndValidateAuthorAccess)
        .then();
  }

  private Mono<Boolean> fetchAndValidateAuthorAccess(String author) {
    return fetchAuthor(author)
        .map(User::hasWriteAccess)
        .map(Boolean::booleanValue)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> ProjectException.authorNotHaveWriteAccess(author)));
  }

  private Mono<User> fetchAuthor(String authorId) {
    return userReactiveClient
        .fetchUser(authorId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(ProjectException.userServiceException(exception)));
  }
}
