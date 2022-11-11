package com.github.devraghav.bugtracker.issue.repository;

import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class InMemoryIssueCommentRepository implements IssueCommentRepository {

  private final Map<String, Set<IssueCommentEntity>> issueCommentsMap = new ConcurrentHashMap<>();

  @Override
  public Mono<IssueCommentEntity> save(String issueId, IssueCommentEntity commentEntity) {
    return Mono.just(commentEntity)
        .flatMap(comment -> addCommentInIssue(issueId, comment))
        .thenReturn(commentEntity);
  }

  private Mono<IssueCommentEntity> addCommentInIssue(
      String issueId, IssueCommentEntity commentEntity) {
    return Mono.just(commentEntity)
        .doOnNext(
            unused -> {
              var comments = issueCommentsMap.get(issueId);
              if (comments != null) {
                comments.add(commentEntity);
              } else {
                issueCommentsMap.put(
                    issueId,
                    new HashSet<>() {
                      {
                        add(commentEntity);
                      }
                    });
              }
            })
        .thenReturn(commentEntity);
  }

  public Flux<IssueCommentEntity> findAllByIssueId(String issueId) {
    return Flux.fromIterable(issueCommentsMap.getOrDefault(issueId, Set.of()));
  }
}
