package com.github.devraghav.bugtracker.issue.user;

import com.github.devraghav.bugtracker.issue.exception.UserClientException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
class DefaultUserClient implements UserClient {
  private final WebClient webClient;

  public DefaultUserClient(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public Mono<User> getUserById(String userId) {
    return webClient
        .get()
        .uri("/api/rest/internal/v1/user/{userId}", userId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(UserClientException.invalidUser(userId)))
        .bodyToMono(User.class)
        .onErrorResume(
            WebClientRequestException.class,
            exception -> Mono.error(UserClientException.unableToConnect(exception)));
  }
}
