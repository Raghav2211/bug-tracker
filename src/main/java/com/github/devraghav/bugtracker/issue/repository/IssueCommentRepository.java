package com.github.devraghav.bugtracker.issue.repository;

import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IssueCommentRepository {

  Mono<IssueCommentEntity> save(String issueId, IssueCommentEntity issueCommentEntity);

  Flux<IssueCommentEntity> findAllByIssueId(String issueId);
}
