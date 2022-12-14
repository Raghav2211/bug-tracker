package com.github.devraghav.bugtracker.project.repository;

import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import reactor.core.publisher.Mono;

public interface ProjectVersionRepository {

  Mono<ProjectVersionEntity> saveVersion(String projectId, ProjectVersionEntity entity);
}
