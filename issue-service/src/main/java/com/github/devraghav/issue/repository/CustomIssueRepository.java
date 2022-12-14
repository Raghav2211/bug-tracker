package com.github.devraghav.issue.repository;

import reactor.core.publisher.Mono;

public interface CustomIssueRepository {
  Mono<Boolean> done(String issuedId);
}
