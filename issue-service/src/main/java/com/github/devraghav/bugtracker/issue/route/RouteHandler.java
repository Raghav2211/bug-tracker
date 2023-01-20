package com.github.devraghav.bugtracker.issue.route;

import java.util.Objects;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public interface RouteHandler {

  default Mono<String> getAuthenticatedPrincipal(ServerRequest request) {
    return request
        .principal()
        .cast(UsernamePasswordAuthenticationToken.class)
        .map(UsernamePasswordAuthenticationToken::getPrincipal)
        .map(Objects::toString);
  }
}
