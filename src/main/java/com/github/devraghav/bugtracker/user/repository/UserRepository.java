package com.github.devraghav.bugtracker.user.repository;

import com.github.devraghav.bugtracker.user.entity.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
  Mono<UserEntity> save(UserEntity entity);

  Mono<UserEntity> findById(String userId);

  Flux<UserEntity> findAll();

  Mono<Boolean> exists(String userId);
}
