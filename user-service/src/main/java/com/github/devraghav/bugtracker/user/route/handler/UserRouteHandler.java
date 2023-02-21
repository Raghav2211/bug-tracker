package com.github.devraghav.bugtracker.user.route.handler;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface UserRouteHandler {

  Mono<ServerResponse> getAll(ServerRequest request);

  Mono<ServerResponse> create(ServerRequest request);

  Mono<ServerResponse> update(ServerRequest request);

  Mono<ServerResponse> get(ServerRequest request);

  Mono<ServerResponse> login(ServerRequest request);
}
