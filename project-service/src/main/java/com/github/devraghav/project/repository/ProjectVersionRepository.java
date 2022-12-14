package com.github.devraghav.project.repository;

import com.github.devraghav.project.entity.ProjectVersionEntity;
import reactor.core.publisher.Mono;

public interface ProjectVersionRepository {

  Mono<ProjectVersionEntity> saveVersion(String projectId, ProjectVersionEntity entity);
}
