package com.github.devraghav.bugtracker.user.route;

import com.github.devraghav.bugtracker.user.dto.*;
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
class UserRouteHandler {
  private final UserService userService;
  private final LoginService loginService;

  Mono<ServerResponse> getAll(ServerRequest request) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(userService.findAll(), User.class);
  }

  Mono<ServerResponse> create(ServerRequest request) {
    return request
        .bodyToMono(UserRequest.Create.class)
        .flatMap(userService::save)
        .flatMap(user -> UserResponse.create(request, user))
        .switchIfEmpty(UserResponse.noBody(request))
        .onErrorResume(UserException.class, exception -> UserResponse.invalid(request, exception));
  }

  Mono<ServerResponse> get(ServerRequest request) {
    return userService
        .findById(request.pathVariable("id"))
        .flatMap(UserResponse::found)
        .onErrorResume(UserException.class, exception -> UserResponse.notFound(request, exception));
  }

  Mono<ServerResponse> login(ServerRequest request) {
    return request
        .bodyToMono(LoginRequest.Request.class)
        .flatMap(loginService::login)
        .flatMap(response -> ServerResponse.ok().body(BodyInserters.fromValue(response)))
        .onErrorResume(
            UserException.UnauthorizedException.class,
            exception -> UserResponse.unauthorizedAccess(request, exception))
        .onErrorResume(UserException.class, exception -> UserResponse.notFound(request, exception));
  }
}
