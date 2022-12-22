package com.github.devraghav.bugtracker.project.service;

import com.github.devraghav.bugtracker.project.dto.User;
import com.github.devraghav.bugtracker.project.dto.UserClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
public class UserReactiveClient {
  private final String userFindByIdURL;
  private final WebClient userWebClient;

  public UserReactiveClient(
      @Value("${app.external.user-service.url}") String userServiceURL, WebClient userWebClient) {
    this.userWebClient = userWebClient;
    this.userFindByIdURL = userServiceURL + "/api/rest/v1/user/{id}";
  }

  public Mono<User> fetchUser(String userId) {
    return userWebClient
        .get()
        .uri(userFindByIdURL, userId)
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
