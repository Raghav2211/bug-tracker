package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.event.internal.UserEvent;
import com.github.devraghav.bugtracker.user.exception.UserException;
import com.github.devraghav.bugtracker.user.mapper.UserMapper;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import com.github.devraghav.bugtracker.user.request.UserRequest;
import com.github.devraghav.bugtracker.user.response.UserResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record UserService(
    RequestValidator requestValidator,
    UserMapper userMapper,
    UserRepository userRepository,
    EventBus.ReactivePublisher<DomainEvent> domainEventReactivePublisher) {

  // @spotless:off
  public Mono<UserResponse.User> save(UserRequest.CreateUser createUserUserRequest) {
    return requestValidator
        .validate(createUserUserRequest)
        .map(userMapper::requestToEntity)
        .flatMap(this::save)
        .onErrorResume(DuplicateKeyException.class,
                exception -> Mono.error(UserException.alreadyExistsWithEmail(createUserUserRequest.email())));
  }
  // @spotless:on

  public Flux<UserResponse.User> findAll() {
    return userRepository.findAll().map(userMapper::entityToResponse);
  }

  public Mono<UserResponse.User> findByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .map(userMapper::entityToResponse)
        .switchIfEmpty(Mono.error(UserException.notFoundByEmail(email)));
  }

  public Mono<UserResponse.User> findById(String userId) {
    return userRepository
        .findById(userId)
        .map(userMapper::entityToResponse)
        .switchIfEmpty(Mono.error(() -> UserException.notFoundById(userId)));
  }

  private Mono<UserResponse.User> save(UserEntity userEntity) {
    return userRepository
        .save(userEntity)
        .map(userMapper::entityToResponse)
        .doOnSuccess(user -> domainEventReactivePublisher.publish(new UserEvent.Created(user)));
  }
}
