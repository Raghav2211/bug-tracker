package com.github.devraghav.bugtracker.project.repository;

import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
record ProjectVersionRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate)
    implements ProjectVersionRepository {
  @Override
  public Mono<ProjectVersionEntity> saveVersion(String projectId, ProjectVersionEntity entity) {
    return reactiveMongoTemplate
        .updateFirst(
            Query.query(Criteria.where("_id").is(projectId)),
            new Update().push("versions").value(entity),
            reactiveMongoTemplate.getCollectionName(ProjectEntity.class))
        .filter(updateResult -> updateResult.getModifiedCount() == 1)
        .map(unused -> entity);
  }
}
