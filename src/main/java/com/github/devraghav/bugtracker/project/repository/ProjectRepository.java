package com.github.devraghav.bugtracker.project.repository;

import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository
    extends ReactiveMongoRepository<ProjectEntity, String>, ProjectVersionRepository {

  @Aggregation({
    "{ '$match' : { '_id' : '?0' } }",
    "{ '$unwind' : '$versions' }",
    "{ '$project': { '_id' : '$versions._id', 'version' : '$versions.version' } }"
  })
  Flux<ProjectVersionEntity> findAllVersionByProjectId(String projectId);

  @ExistsQuery("{ '$and' :[ {'_id' :'?0' }, { 'versions._id' : '?1' } ] }")
  Mono<Boolean> existsByIdAndVersionId(String projectId, String versionId);
}
