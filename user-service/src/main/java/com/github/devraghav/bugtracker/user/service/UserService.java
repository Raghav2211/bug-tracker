package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.event.internal.UserEvent;
import com.github.devraghav.bugtracker.user.mapper.UserMapper;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import com.github.devraghav.bugtracker.user.validation.RequestValidator;
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
  public Mono<User> save(UserRequest.Create createUserRequest) {
    return requestValidator
        .validate(createUserRequest)
        .map(userMapper::requestToEntity)
        .flatMap(this::save)
        .onErrorResume(DuplicateKeyException.class,
                exception -> Mono.error(UserException.alreadyExistsWithEmail(createUserRequest.email())));
  }
  // @spotless:on

  public Flux<User> findAll() {
    return userRepository.findAll().map(userMapper::entityToResponse);
  }

  public Mono<User> findByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .map(userMapper::entityToResponse)
        .switchIfEmpty(Mono.error(UserException.notFoundByEmail(email)));
  }

  public Mono<User> findById(String userId) {
    return userRepository
        .findById(userId)
        .map(userMapper::entityToResponse)
        .switchIfEmpty(Mono.error(() -> UserException.notFoundById(userId)));
  }

  private Mono<User> save(UserEntity userEntity) {
    return userRepository
        .save(userEntity)
        .map(userMapper::entityToResponse)
        .doOnSuccess(user -> domainEventReactivePublisher.publish(new UserEvent.Created(user)));
  }
}
