package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public Mono<User> save(UserRequest userRequest) {
    return Mono.just(userRequest)
        .map(UserEntity::new)
        .flatMap(userRepository::save)
        .map(User::new)
        .onErrorResume(
            DuplicateKeyException.class,
            exception -> Mono.error(UserAlreadyExistsException.withEmail(userRequest.getEmail())));
  }

  public Mono<Boolean> exists(String userId) {
    return userRepository
        .existsById(userId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(UserException.notFound(userId)));
  }

  public Mono<User> findById(String userId) {
    return userRepository
        .findById(userId)
        .map(User::new)
        .switchIfEmpty(Mono.error(() -> new UserNotFoundException(userId)));
  }

  public Mono<Boolean> hasUserWriteAccess(String userId) {
    return findById(userId).map(User::isWriteAccess).map(Boolean::booleanValue);
  }
}
