package com.github.devraghav.bugtracker.project.repository;

import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class InMemoryProjectRepository implements ProjectRepository {
  private final Map<String, ProjectEntity> projectDB;
  private final Set<String> projectNameList;

  public InMemoryProjectRepository() {
    projectDB = new ConcurrentHashMap<>();
    projectNameList = new ConcurrentSkipListSet<>();
  }

  @Override
  public Mono<ProjectEntity> save(ProjectEntity entity) {
    return Mono.just(entity)
        .filter(entity1 -> !projectNameList.contains(entity.getName()))
        .switchIfEmpty(Mono.error(() -> ProjectAlreadyExistsException.withName(entity.getName())))
        .map(this::addProject);
  }

  @Override
  public Mono<ProjectEntity> findById(String id) {
    return Mono.just(id)
        .mapNotNull(projectDB::get)
        .switchIfEmpty(Mono.error(() -> new ProjectNotFoundException(id)));
  }

  @Override
  public Flux<ProjectEntity> findAll() {
    return Flux.fromIterable(projectDB.values());
  }

  @Override
  public Mono<Boolean> exists(String id) {
    return findById(id).map(found -> true).onErrorReturn(ProjectNotFoundException.class, false);
  }

  private ProjectEntity addProject(ProjectEntity entity) {
    projectNameList.add(entity.getName());
    projectDB.put(entity.getId(), entity);
    return entity;
  }
}
