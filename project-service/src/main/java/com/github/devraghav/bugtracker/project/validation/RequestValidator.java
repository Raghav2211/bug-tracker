package com.github.devraghav.bugtracker.project.validation;

import com.github.devraghav.bugtracker.project.dto.ProjectException;
import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import com.github.devraghav.bugtracker.project.dto.User;
import com.github.devraghav.bugtracker.project.dto.UserClientException;
import com.github.devraghav.bugtracker.project.service.UserReactiveClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RequestValidator {
  private final Validator<ProjectRequest.Create> createUserRequestValidator;
  private final UserReactiveClient userReactiveClient;

  public Mono<ProjectRequest.Create> validate(
      final String author, final ProjectRequest.Create request) {
    return Mono.zip(createUserRequestValidator.validate(request), validateAuthor(author))
        .thenReturn(request);
  }

  public Mono<String> validateAuthor(final String author) {
    return Mono.justOrEmpty(author)
        .filter(StringUtils::hasLength)
        .switchIfEmpty(Mono.error(ProjectException::nullAuthor))
        .flatMap(this::fetchAndValidateAuthorAccess)
        .thenReturn(author);
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
