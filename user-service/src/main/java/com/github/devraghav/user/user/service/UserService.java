package com.github.devraghav.user.user.service;

import com.github.devraghav.user.user.dto.User;
import com.github.devraghav.user.user.dto.UserException;
import com.github.devraghav.user.user.dto.UserRequest;
import com.github.devraghav.user.user.mapper.UserMapper;
import com.github.devraghav.user.user.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record UserService(UserRepository userRepository) {

  public Mono<User> save(UserRequest userRequest) {
    return Mono.just(userRequest)
        .map(UserMapper.INSTANCE::requestToEntity)
        .flatMap(userRepository::save)
        .map(UserMapper.INSTANCE::entityToResponse)
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(UserException.alreadyExistsWithEmail(userRequest.email())));
  }

  public Flux<User> findAll() {
    return userRepository.findAll().map(UserMapper.INSTANCE::entityToResponse);
  }

  public Mono<User> findById(String userId) {
    return userRepository
        .findById(userId)
        .map(UserMapper.INSTANCE::entityToResponse)
        .switchIfEmpty(Mono.error(() -> UserException.notFound(userId)));
  }
}
