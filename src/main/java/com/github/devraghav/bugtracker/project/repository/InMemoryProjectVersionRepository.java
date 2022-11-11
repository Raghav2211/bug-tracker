package com.github.devraghav.bugtracker.project.repository;

import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class InMemoryProjectVersionRepository implements ProjectVersionRepository {
  private final Map<String, List<ProjectVersionEntity>> versionDB;

  public InMemoryProjectVersionRepository() {
    versionDB = new ConcurrentHashMap<>();
  }

  @Override
  public Mono<ProjectVersionEntity> save(String projectId, ProjectVersionEntity entity) {
    return Mono.just(entity)
        .map(
            versionEntity -> {
              var projectVersions = versionDB.get(projectId);
              if (projectVersions == null) {
                versionDB.put(
                    projectId,
                    new ArrayList<>() {
                      {
                        add(entity);
                      }
                    });

              } else {
                projectVersions.add(entity);
              }
              return entity;
            });
  }

  @Override
  public Flux<ProjectVersionEntity> findAll(String projectId) {
    return Flux.fromIterable(versionDB.getOrDefault(projectId, List.of()));
  }

  @Override
  public Mono<Boolean> exists(String projectId, String versionId) {
    return Mono.just(projectId)
        .map(versionDB::get)
        .map(
            versionEntities ->
                versionEntities.stream()
                    .anyMatch(versionEntity -> versionEntity.getId().equals(versionId)));
  }
}
