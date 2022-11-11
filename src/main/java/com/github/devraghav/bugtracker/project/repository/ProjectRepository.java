package com.github.devraghav.bugtracker.project.repository;

import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectRepository {
  Mono<ProjectEntity> save(ProjectEntity entity);

  Mono<ProjectEntity> findById(String id);

  Flux<ProjectEntity> findAll();

  Mono<Boolean> exists(String id);
}
