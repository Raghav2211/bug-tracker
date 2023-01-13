package com.github.devraghav.bugtracker.issue.repository;

import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface IssueRepository
    extends ReactiveMongoRepository<IssueEntity, String>,
        ReactiveSortingRepository<IssueEntity, String> {

  @Update("{ '$set' : { 'assignee' : '?1' } }")
  Mono<Long> findAndSetAssigneeById(String id, String userId);

  @Update("{ '$unset' : { 'assignee' : '' } }")
  Mono<Long> findAndUnSetAssigneeById(String id);

  @Update("{ '$addToSet' : { 'watchers' : '?1' } }")
  Mono<Long> findAndAddWatcherById(String id, String userId);

  @Update("{ '$pull' : { 'watchers' : '?1' } }")
  Mono<Long> findAndPullWatcherById(String id, String userId);

  @Query("{ 'projects.projectId' : '?0'}")
  Flux<IssueEntity> findAllByProjectId(String projectId);

  Flux<IssueEntity> findAllByReporter(String reporter);

  @Update("{ '$set' : { 'endedAt' : '?1' } }")
  Mono<Long> findAndSetEndedAtById(String id, LocalDateTime endedAt);

  Flux<IssueEntity> findAllBy(Pageable pageable);
}
