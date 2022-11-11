package com.github.devraghav.bugtracker.issue.repository;

import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IssueRepository {

  Mono<IssueEntity> save(IssueEntity entity);

  Mono<IssueEntity> findById(String issueId);

  Flux<IssueEntity> findAllByProjectId(String projectId);

  Flux<IssueEntity> findAllByReporter(String reporter);

  Mono<Boolean> assign(String issuedId, String userId);

  Mono<Boolean> unassign(String issuedId, String userId);

  Mono<Boolean> addWatcher(String issuedId, String userId);

  Mono<Boolean> removeWatcher(String issuedId, String userId);

  Mono<Boolean> done(String issuedId);

  Flux<IssueEntity> findAll();

  Mono<Boolean> exists(String issueId);
}
