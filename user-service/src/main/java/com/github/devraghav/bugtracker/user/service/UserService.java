package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.event.internal.UserCreatedEvent;
import com.github.devraghav.bugtracker.user.mapper.UserMapper;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import com.github.devraghav.bugtracker.user.validation.RequestValidator;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record UserService(
    RequestValidator requestValidator,
    UserMapper userMapper,
    UserRepository userRepository,
    UserAPIEventHandler userAPIEventHandler) {

  public Mono<User> save(CreateUserRequest createUserRequest) {
    return requestValidator
        .validate(createUserRequest)
        .map(userMapper::requestToEntity)
        .flatMap(this::save)
        .onErrorResume(DuplicateKeyException.class, exception -> duplicateUser(createUserRequest));
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

  private Mono<User> save(UserEntity userEntity) {
    return userRepository
        .save(userEntity)
        .map(userMapper::entityToResponse)
        .flatMap(
            user ->
                userAPIEventHandler.handleUserCreated(getUserCreatedEvent(user)).thenReturn(user));
  }

  private UserCreatedEvent getUserCreatedEvent(User user) {
    return new UserCreatedEvent(UUID.randomUUID(), user, LocalDateTime.now());
  }

  private Mono<User> duplicateUser(CreateUserRequest createUserRequest) {
    return userAPIEventHandler
        .handleUserDuplicated(createUserRequest)
        .flatMap(
            unused -> Mono.error(UserException.alreadyExistsWithEmail(createUserRequest.email())));
  }
}
