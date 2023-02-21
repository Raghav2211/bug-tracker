package com.github.devraghav.bugtracker.issue.project;

import reactor.core.publisher.Mono;

public interface ProjectClient {

  Mono<ProjectResponse.Project> getProjectById(String projectId);

  Mono<ProjectResponse.Project.Version> getVersionById(String projectId, String versionId);
}
