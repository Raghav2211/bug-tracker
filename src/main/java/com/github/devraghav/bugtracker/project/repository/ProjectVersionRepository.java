package com.github.devraghav.bugtracker.project.repository;

import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectVersionRepository {

  Mono<ProjectVersionEntity> save(String projectId, ProjectVersionEntity entity);

  Flux<ProjectVersionEntity> findAll(String projectId);

  Mono<Boolean> exists(String projectId, String versionId);
}
