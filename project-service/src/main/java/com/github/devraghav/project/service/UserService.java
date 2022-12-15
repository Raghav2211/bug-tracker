package com.github.devraghav.project.service;

import com.github.devraghav.project.dto.ProjectException;
import com.github.devraghav.project.dto.User;
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

  public Mono<User> getUserById(String authorId) {
    return webClient
        .get()
        .uri(userFindByIdURL, authorId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(ProjectException.authorNotFound(authorId)))
        .bodyToMono(User.class);
  }
}
