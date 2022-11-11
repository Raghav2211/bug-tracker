package com.github.devraghav.bugtracker.issue.repository;

import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class InMemoryIssueRepository implements IssueRepository {
  private Map<String, IssueEntity> issueDB;

  public InMemoryIssueRepository() {
    issueDB = new ConcurrentHashMap<>();
  }

  @Override
  public Mono<IssueEntity> save(IssueEntity entity) {
    return Mono.just(entity).map(this::addIssue);
  }

  @Override
  public Mono<IssueEntity> findById(String issueId) {
    return Mono.just(issueId)
        .mapNotNull(issueDB::get)
        .switchIfEmpty(Mono.error(() -> new IssueNotFoundException(issueId)));
  }

  @Override
  public Flux<IssueEntity> findAllByProjectId(String projectId) {
    return Flux.fromIterable(issueDB.values())
        .filter(
            issueEntity ->
                issueEntity.getProjects().stream()
                    .anyMatch(projectInfoRef -> projectId.equals(projectInfoRef.getProjectId())));
  }

  @Override
  public Flux<IssueEntity> findAllByReporter(String reporter) {
    return Flux.fromIterable(issueDB.values())
        .filter(issueEntity -> reporter.equals(issueEntity.getReporter()));
  }

  @Override
  public Mono<Boolean> assign(String issuedId, String userId) {
    return findById(issuedId)
        .doOnNext(issueEntity -> issueEntity.setAssignee(userId))
        .thenReturn(true);
  }

  @Override
  public Mono<Boolean> unassign(String issuedId, String userId) {
    return findById(issuedId)
        .doOnNext(issueEntity -> issueEntity.setAssignee(null))
        .thenReturn(true);
  }

  @Override
  public Mono<Boolean> addWatcher(String issuedId, String userId) {
    return findById(issuedId).map(issueEntity -> issueEntity.getWatchers().add(userId));
  }

  @Override
  public Mono<Boolean> removeWatcher(String issuedId, String userId) {
    return findById(issuedId)
        .map(issueEntity -> issueEntity.getWatchers().removeIf(userId::equals));
  }

  @Override
  public Mono<Boolean> done(String issuedId) {
    return findById(issuedId)
        .map(
            issueEntity -> {
              issueEntity.setEndedAt(LocalDateTime.now());
              return issueEntity;
            })
        .thenReturn(true);
  }

  @Override
  public Flux<IssueEntity> findAll() {
    return Flux.fromIterable(issueDB.values());
  }

  @Override
  public Mono<Boolean> exists(String issueId) {
    return findById(issueId).map(found -> true).onErrorReturn(IssueNotFoundException.class, false);
  }

  private IssueEntity addIssue(IssueEntity entity) {
    issueDB.put(entity.getId(), entity);
    return entity;
  }
}
