package com.github.devraghav.bugtracker.issue.project;

import com.github.devraghav.bugtracker.issue.exception.ProjectClientException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Component
class DefaultProjectClient implements ProjectClient {
  private final WebClient webClient;

  public DefaultProjectClient(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public Mono<ProjectResponse.Project> getProjectById(String projectId) {
    return webClient
        .get()
        .uri("/api/rest/internal/v1/project/{projectId}", projectId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(ProjectClientException.invalidProject(projectId)))
        .bodyToMono(ProjectResponse.Project.class)
        .onErrorResume(
            WebClientRequestException.class,
            exception -> Mono.error(ProjectClientException.unableToConnect(exception)));
  }

  @Override
  public Mono<ProjectResponse.Project.Version> getVersionById(String projectId, String versionId) {
    return webClient
        .get()
        .uri("/api/rest/internal/v1/project/{projectId}/version/{versionId}", projectId, versionId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(ProjectClientException.invalidVersion(versionId)))
        .bodyToMono(ProjectResponse.Project.Version.class)
        .onErrorResume(
            WebClientRequestException.class,
            exception -> Mono.error(ProjectClientException.unableToConnect(exception)));
  }
}
