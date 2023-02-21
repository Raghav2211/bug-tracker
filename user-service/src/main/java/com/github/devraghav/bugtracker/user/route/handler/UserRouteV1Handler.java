package com.github.devraghav.bugtracker.user.route.handler;

import com.github.devraghav.bugtracker.user.exception.UserException;
import com.github.devraghav.bugtracker.user.request.UserRequest;
import com.github.devraghav.bugtracker.user.response.UserResponse;
import com.github.devraghav.bugtracker.user.service.LoginService;
import com.github.devraghav.bugtracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
class UserRouteV1Handler implements UserRouteHandler {
  private final UserService userService;
  private final LoginService loginService;

  public Mono<ServerResponse> getAll(ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(userService.findAll(), UserResponse.User.class);
  }

  @Override
  public Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(UserRequest.CreateUser.class)
        .flatMap(userService::save)
        .flatMap(user -> UserResponse.create(request, user))
        .switchIfEmpty(UserResponse.noBody(request))
        .onErrorResume(UserException.class, exception -> UserResponse.invalid(request, exception));
  }

  @Override
  public Mono<ServerResponse> update(ServerRequest request) {
    var userId = request.pathVariable("id");
    return request
        .bodyToMono(UserRequest.UpdateUser.class)
        .flatMap(updateUser -> userService.update(userId, updateUser))
        .flatMap(user -> UserResponse.create(request, user))
        .switchIfEmpty(UserResponse.noBody(request))
        .onErrorResume(UserException.class, exception -> UserResponse.invalid(request, exception));
  }

  @Override
  public Mono<ServerResponse> get(ServerRequest request) {
    return userService
        .findById(request.pathVariable("id"))
        .flatMap(UserResponse::found)
        .onErrorResume(UserException.class, exception -> UserResponse.notFound(request, exception));
  }

  @Override
  public Mono<ServerResponse> login(ServerRequest request) {
    return request
        .bodyToMono(UserRequest.AuthRequest.class)
        .flatMap(loginService::login)
        .flatMap(response -> ServerResponse.ok().body(BodyInserters.fromValue(response)))
        .onErrorResume(
            UserException.UnauthorizedException.class,
            exception -> UserResponse.unauthorizedAccess(request, exception))
        .onErrorResume(UserException.class, exception -> UserResponse.notFound(request, exception));
  }
}
