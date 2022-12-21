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
public record ProjectVersionRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate)
    implements ProjectVersionRepository {
  @Override
  public Mono<ProjectVersionEntity> saveVersion(String projectId, ProjectVersionEntity entity) {

    return reactiveMongoTemplate.findAndModify(
        Query.query(Criteria.where("_id").is(projectId)),
        new Update().push("versions").value(entity),
        ProjectVersionEntity.class,
        reactiveMongoTemplate.getCollectionName(ProjectEntity.class));
  }
}
