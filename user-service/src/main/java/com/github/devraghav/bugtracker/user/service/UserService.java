package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.kafka.producer.KafkaProducer;
import com.github.devraghav.bugtracker.user.mapper.UserMapper;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record UserService(
    UserMapper userMapper, UserRepository userRepository, KafkaProducer kafkaProducer) {

  public Mono<User> save(String requestId, UserRequest userRequest) {
    return kafkaProducer
        .sendUserCreateCommand(requestId, userRequest)
        .map(userMapper::requestToEntity)
        .flatMap(userEntity -> save(requestId, userEntity))
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> duplicateUser(requestId, userRequest, exception));
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

  private Mono<User> save(String requestId, UserEntity userEntity) {
    return userRepository
        .save(userEntity)
        .map(userMapper::entityToResponse)
        .flatMap(user -> kafkaProducer.sendUserCreatedEvent(requestId, user));
  }

  private Mono<User> duplicateUser(
      String requestId, UserRequest userRequest, DuplicateKeyException exception) {
    return kafkaProducer
        .sendUserDuplicatedEvent(requestId, userRequest)
        .flatMap(unused -> Mono.error(UserException.alreadyExistsWithEmail(userRequest.email())));
  }
}
