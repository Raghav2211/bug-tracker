package com.github.devraghav.bugtracker.issue.route.handler;

import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface IssueRouteHandler extends RouteHandler {

  Mono<ServerResponse> getAll(ServerRequest serverRequest);

  Mono<ServerResponse> create(ServerRequest request);

  Mono<ServerResponse> update(ServerRequest request);

  Mono<ServerResponse> get(ServerRequest request);

  Mono<ServerResponse> monitor(ServerRequest request, IssueRequest.MonitorType monitorType);

  Mono<ServerResponse> addAttachment(ServerRequest request);

  Mono<ServerResponse> resolve(ServerRequest request);
}
