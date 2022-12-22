package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.Project;
import com.github.devraghav.bugtracker.issue.dto.ProjectClientException;
import com.github.devraghav.bugtracker.issue.dto.ProjectVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Service
public class ProjectReactiveClient {
  private final String projectFindByIdURL;
  private final String projectVersionFindByIdURL;
  private final WebClient projectWebClient;

  public ProjectReactiveClient(
      @Value("${app.external.project-service.url}") String projectServiceURL,
      WebClient projectWebClient) {
    this.projectWebClient = projectWebClient;
    this.projectFindByIdURL = projectServiceURL + "/api/rest/v1/project/{id}";
    this.projectVersionFindByIdURL =
        projectServiceURL + "/api/rest/v1/project/{id}/version/{versionId}";
  }

  public Mono<Project> fetchProject(String projectId) {
    return projectWebClient
        .get()
        .uri(projectFindByIdURL, projectId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(ProjectClientException.invalidProject(projectId)))
        .bodyToMono(Project.class)
        .onErrorResume(
            WebClientRequestException.class,
            exception -> Mono.error(ProjectClientException.unableToConnect(exception)));
  }

  public Mono<Boolean> isProjectExists(String projectId) {
    return fetchProject(projectId).hasElement();
  }

  public Mono<ProjectVersion> fetchProjectVersion(String projectId, String versionId) {
    return projectWebClient
        .get()
        .uri(projectVersionFindByIdURL, projectId, versionId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse ->
                Mono.error(ProjectClientException.invalidProjectOrVersion(projectId, versionId)))
        .bodyToMono(ProjectVersion.class)
        .onErrorResume(
            WebClientRequestException.class,
            exception -> Mono.error(ProjectClientException.unableToConnect(exception)));
  }

  public Mono<Boolean> isProjectVersionExists(String projectId, String versionId) {
    return fetchProjectVersion(projectId, versionId).hasElement();
  }
}
