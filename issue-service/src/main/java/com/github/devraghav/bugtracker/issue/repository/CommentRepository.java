package com.github.devraghav.bugtracker.issue.repository;

import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CommentRepository extends ReactiveMongoRepository<CommentEntity, String> {
  Flux<CommentEntity> findAllByIssueId(String issueId);
}
