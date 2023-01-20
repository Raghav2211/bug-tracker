package com.github.devraghav.bugtracker.project.route.handler;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface ProjectRouteHandler {

  Mono<ServerResponse> getAllProjects(ServerRequest serverRequest);

  Mono<ServerResponse> createProject(ServerRequest request);

  Mono<ServerResponse> getProject(ServerRequest request);

  Mono<ServerResponse> addVersionToProject(ServerRequest request);

  Mono<ServerResponse> getAllProjectVersion(ServerRequest request);

  Mono<ServerResponse> getProjectVersion(ServerRequest request);

  default Mono<String> getAuthenticatedPrincipal(ServerRequest request) {
    return request
        .principal()
        .cast(UsernamePasswordAuthenticationToken.class)
        .map(UsernamePasswordAuthenticationToken::getPrincipal)
        .map(String::valueOf);
  }
}
