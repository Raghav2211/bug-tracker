package com.github.devraghav.issue.service;

import com.github.devraghav.issue.dto.IssueException;
import com.github.devraghav.issue.dto.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private final String userFindByIdURL;
  private final WebClient webClient;

  public UserService(
      @Value("${app.external.user-service.url}") String userServiceURL, WebClient webClient) {
    this.webClient = webClient;
    this.userFindByIdURL = userServiceURL + "/api/rest/v1/user/{id}";
  }

  public Mono<User> fetchUser(String userId) {
    return webClient
        .get()
        .uri(userFindByIdURL, userId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(IssueException.invalidUser(userId)))
        .bodyToMono(User.class);
  }
}
