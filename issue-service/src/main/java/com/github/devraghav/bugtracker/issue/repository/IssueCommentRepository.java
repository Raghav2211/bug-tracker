package com.github.devraghav.bugtracker.issue.repository;

import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IssueCommentRepository
    extends ReactiveMongoRepository<IssueCommentEntity, String> {
  Flux<IssueCommentEntity> findAllByIssueId(String issueId);
}
