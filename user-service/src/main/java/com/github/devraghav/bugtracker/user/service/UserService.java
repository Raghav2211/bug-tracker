package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.user.event.internal.UserCreatedEvent;
import com.github.devraghav.bugtracker.user.event.internal.UserDuplicatedEvent;
import com.github.devraghav.bugtracker.user.mapper.UserMapper;
import com.github.devraghav.bugtracker.user.pubsub.ReactivePublisher;
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
    ReactivePublisher<DomainEvent> domainEventReactivePublisher) {

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
                domainEventReactivePublisher.publish(new UserCreatedEvent(user)).thenReturn(user));
  }

  private Mono<User> duplicateUser(CreateUserRequest createUserRequest) {
    return domainEventReactivePublisher
        .publish(new UserDuplicatedEvent(createUserRequest))
        .thenReturn(createUserRequest)
        .flatMap(
            duplicateRequest ->
                Mono.error(UserException.alreadyExistsWithEmail(duplicateRequest.email())));
  }
}
