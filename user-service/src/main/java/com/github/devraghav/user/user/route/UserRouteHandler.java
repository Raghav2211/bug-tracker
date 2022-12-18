package com.github.devraghav.user.user.route;

import com.github.devraghav.user.user.dto.User;
import com.github.devraghav.user.user.dto.UserException;
import com.github.devraghav.user.user.dto.UserRequest;
import com.github.devraghav.user.user.dto.UserResponse;
import com.github.devraghav.user.user.repository.UserRepository;
import com.github.devraghav.user.user.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public record UserRouteHandler(UserService userService, UserRepository userRepository) {
  public Mono<ServerResponse> getAll(ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(userService.findAll(), User.class);
  }

  public Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(UserRequest.class)
        .flatMap(UserRequest::validate)
        .flatMap(userService::save)
        .flatMap(user -> UserResponse.create(request, user))
        .switchIfEmpty(UserResponse.noBody(request))
        .onErrorResume(UserException.class, exception -> UserResponse.invalid(request, exception));
  }

  public Mono<ServerResponse> get(ServerRequest request) {
    return userService
        .findById(request.pathVariable("id"))
        .flatMap(UserResponse::found)
        .onErrorResume(UserException.class, exception -> UserResponse.notFound(request, exception));
  }
}