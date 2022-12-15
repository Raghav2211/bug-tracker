package com.github.devraghav.issue.service;

import com.github.devraghav.issue.dto.IssueException;
import com.github.devraghav.issue.dto.Project;
import com.github.devraghav.issue.dto.ProjectVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ProjectService {
  private final String projectFindByIdURL;
  private final String projectVersionFindByIdURL;
  private final WebClient webClient;

  public ProjectService(
      @Value("${app.external.project-service.url}") String projectServiceURL, WebClient webClient) {
    this.webClient = webClient;
    this.projectFindByIdURL = projectServiceURL + "/api/rest/v1/project/{id}";
    this.projectVersionFindByIdURL =
        projectServiceURL + "/api/rest/v1/project/{id}/version/{versionId}";
  }

  public Mono<Project> fetchProject(String projectId) {
    return webClient
        .get()
        .uri(projectFindByIdURL, projectId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(IssueException.invalidProject(projectId)))
        .bodyToMono(Project.class);
  }

  public Mono<ProjectVersion> fetchProjectVersion(String projectId, String versionId) {
    return webClient
        .get()
        .uri(projectVersionFindByIdURL, projectId, versionId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(IssueException.invalidProject(projectId)))
        .bodyToMono(ProjectVersion.class);
  }
}
