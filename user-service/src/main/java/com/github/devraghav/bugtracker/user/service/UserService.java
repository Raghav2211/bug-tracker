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
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

  private final RequestValidator requestValidator;
  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final EventBus.ReactivePublisher<DomainEvent> domainEventPublisher;

  public Mono<UserResponse.User> save(UserRequest.CreateUser createUser) {
    // @spotless:off
    return requestValidator
        .validate(createUser)
        .map(userMapper::requestToEntity)
        .flatMap(userEntity -> upsert(userEntity,user -> domainEventPublisher.publish(new UserEvent.Created(user))))
        .onErrorResume(DuplicateKeyException.class,
                exception -> Mono.error(UserException.alreadyExistsWithEmail(createUser.email())));
    // @spotless:on
  }

  public Mono<UserResponse.User> update(String userId, UserRequest.UpdateUser updateUser) {
    // @spotless:off
    return requestValidator
        .validate(updateUser)
        .zipWith(userRepository.findById(userId))
        .map(tuple2 -> userMapper.requestToEntity(tuple2.getT2(), tuple2.getT1()))
        .flatMap(userEntity ->
                upsert(userEntity, user -> domainEventPublisher.publish(new UserEvent.Updated(user))));
    // @spotless:on
  }

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

  private Mono<UserResponse.User> upsert(
      UserEntity userEntity, Consumer<UserResponse.User> onSuccessConsumer) {
    return userRepository
        .save(userEntity)
        .map(userMapper::entityToResponse)
        .doOnSuccess(onSuccessConsumer);
  }
}
