package com.github.devraghav.bugtracker.user.route;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.dto.UserResponse;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import com.github.devraghav.bugtracker.user.service.UserAlreadyExistsException;
import com.github.devraghav.bugtracker.user.service.UserNotFoundException;
import com.github.devraghav.bugtracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserRouteHandler {
  private final UserService userService;
  private final UserRepository userRepository;

  public Mono<ServerResponse> getAll(ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(userRepository.findAll().map(User::new), User.class);
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(UserRequest.class)
        .flatMap(userRequest -> userRequest.validate(userRequest))
        .flatMap(userService::save)
        .flatMap(user -> UserResponse.create(request, user))
        .switchIfEmpty(UserResponse.noBody(request))
        .onErrorResume(UserException.class, exception -> UserResponse.invalid(request, exception))
        .onErrorResume(
            UserAlreadyExistsException.class,
            exception -> UserResponse.alreadyExists(request, exception));
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    var userId = request.pathVariable("id");
    return userService
        .findById(userId)
        .flatMap(UserResponse::found)
        .onErrorResume(
            UserNotFoundException.class, exception -> UserResponse.notFound(request, exception));
  }
}
