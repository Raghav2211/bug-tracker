package com.github.devraghav.bugtracker.issue.user;

import reactor.core.publisher.Mono;

public interface UserClient {
  Mono<User> getUserById(String userId);
}
