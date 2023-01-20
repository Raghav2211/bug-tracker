package com.github.devraghav.bugtracker.issue.route;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface CommentRouteHandler extends RouteHandler {

  public Mono<ServerResponse> getAll(ServerRequest serverRequest);

  public Mono<ServerResponse> save(ServerRequest request);

  public Mono<ServerResponse> update(ServerRequest request);

  public Mono<ServerResponse> get(ServerRequest request);

  public Mono<ServerResponse> subscribeCommentStream(ServerRequest request);
}
