package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.mapper.UserMapper;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record UserService(UserMapper userMapper, UserRepository userRepository) {

  public Mono<User> save(UserRequest userRequest) {
    return Mono.just(userRequest)
        .map(userMapper::requestToEntity)
        .flatMap(userRepository::save)
        .map(userMapper::entityToResponse)
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(UserException.alreadyExistsWithEmail(userRequest.email())));
  }

  public Flux<User> findAll() {
    return userRepository.findAll().map(userMapper::entityToResponse);
  }

  public Mono<User> findById(String userId) {
    return userRepository
        .findById(userId)
        .map(userMapper::entityToResponse)
        .switchIfEmpty(Mono.error(() -> UserException.notFound(userId)));
  }
}
