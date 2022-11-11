package com.github.devraghav.bugtracker.user.repository;

import com.github.devraghav.bugtracker.user.entity.UserEntity;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class InMemoryUserRepository implements UserRepository {
  private final Map<String, UserEntity> userDB = new ConcurrentHashMap<>();
  private final Set<String> emailSet = new ConcurrentSkipListSet<>();

  @Override
  public Mono<UserEntity> save(UserEntity entity) {
    return Mono.just(entity)
        .filter(user -> !emailSet.contains(user.getEmail()))
        .switchIfEmpty(Mono.error(() -> UserAlreadyExistsException.withEmail(entity.getEmail())))
        .map(this::addUser)
        .doOnNext(userEntity -> emailSet.add(userEntity.getEmail()));
  }

  @Override
  public Mono<UserEntity> findById(String userId) {
    return Mono.just(userId)
        .mapNotNull(userDB::get)
        .switchIfEmpty(Mono.error(() -> new UserNotFoundException(userId)));
  }

  @Override
  public Flux<UserEntity> findAll() {
    return Flux.fromIterable(userDB.values());
  }

  @Override
  public Mono<Boolean> exists(String userId) {
    return findById(userId).map(found -> true).onErrorReturn(UserNotFoundException.class, false);
  }

  private UserEntity addUser(UserEntity userEntity) {
    userDB.put(userEntity.getId(), userEntity);
    return userEntity;
  }
}
