package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public Mono<Boolean> exists(String userId) {
    return userRepository
        .exists(userId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(UserException.notFound(userId)));
  }

  public Mono<User> findById(String userId) {
    return userRepository.findById(userId).map(User::new);
  }

  public Mono<Boolean> hasUserWriteAccess(String userId) {
    return findById(userId).map(User::isWriteAccess).map(Boolean::booleanValue);
  }
}
