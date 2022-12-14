package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record UserService(UserRepository userRepository) {

  public Mono<User> save(UserRequest userRequest) {
    return Mono.just(userRequest)
        .map(UserEntity::new)
        .flatMap(userRepository::save)
        .map(User::new)
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(UserException.alreadyExistsWithEmail(userRequest.getEmail())));
  }

  public Flux<User> findAll() {
    return userRepository.findAll().map(User::new);
  }

  public Mono<User> findById(String userId) {
    return userRepository
        .findById(userId)
        .map(User::new)
        .switchIfEmpty(Mono.error(() -> UserException.notFound(userId)));
  }
}
