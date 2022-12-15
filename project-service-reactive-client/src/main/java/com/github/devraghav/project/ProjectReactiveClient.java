package com.github.devraghav.project;

import com.github.devraghav.project.dto.Project;
import com.github.devraghav.project.dto.ProjectClientException;
import com.github.devraghav.project.dto.ProjectVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ProjectReactiveClient {
  private final String projectFindByIdURL;
  private final String projectVersionFindByIdURL;
  private final WebClient webClient;

  public ProjectReactiveClient(
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
            clientResponse -> Mono.error(ProjectClientException.invalidProject(projectId)))
        .bodyToMono(Project.class);
  }

  public Mono<Boolean> isProjectExists(String projectId) {
    return fetchProject(projectId).hasElement();
  }

  public Mono<ProjectVersion> fetchProjectVersion(String projectId, String versionId) {
    return webClient
        .get()
        .uri(projectVersionFindByIdURL, projectId, versionId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse ->
                Mono.error(ProjectClientException.invalidProjectOrVersion(projectId, versionId)))
        .bodyToMono(ProjectVersion.class);
  }

  public Mono<Boolean> isProjectVersionExists(String projectId, String versionId) {
    return fetchProjectVersion(projectId, versionId).hasElement();
  }
}
