package com.github.devraghav.bugtracker.issue.repository;

import reactor.core.publisher.Mono;

public interface CustomIssueRepository {
  Mono<Boolean> done(String issuedId);
}
