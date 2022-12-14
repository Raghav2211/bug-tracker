package com.github.devraghav.issue.repository;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

public record CustomIssueRepositoryImpl(ReactiveMongoTemplate reactiveMongoTemplate)
    implements CustomIssueRepository {
  @Override
  public Mono<Boolean> done(String issuedId) {
    return null;
  }
}
