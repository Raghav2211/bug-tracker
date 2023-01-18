package com.github.devraghav.bugtracker.issue.security;

import io.jsonwebtoken.Claims;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final JWTService jwtService;

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    // spotless:off
    return http.authorizeExchange()
            .pathMatchers("/actuator/**")
            .permitAll()
            .anyExchange()
            .authenticated()
            .and()
            .formLogin()
            .disable()
            .csrf()
            .disable()
            .httpBasic()
            .disable()
            .authenticationManager(reactiveAuthenticationManager(jwtService))
            .securityContextRepository(securityContextRepository(reactiveAuthenticationManager(jwtService)))
            .exceptionHandling()
            .authenticationEntryPoint((serverWebExchange, authenticationException) -> Mono.fromRunnable(() ->serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
            .accessDeniedHandler((serverWebExchange, authenticationException) -> Mono.fromRunnable(() -> serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
            .and()
            .build();
    // spotless:on
  }

  @Bean
  public ReactiveAuthenticationManager reactiveAuthenticationManager(JWTService jwtService) {
    return new AuthenticationManager(jwtService);
  }

  @Bean
  public ServerSecurityContextRepository securityContextRepository(
      ReactiveAuthenticationManager authenticationManager) {
    return new SecurityContextRepository(authenticationManager);
  }

  @RequiredArgsConstructor
  static class AuthenticationManager implements ReactiveAuthenticationManager {
    private final JWTService jwtService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
      String authToken = authentication.getCredentials().toString();
      // spotless:off
      return Mono.just(jwtService.validateToken(authToken))
              .filter(Boolean::booleanValue)
              .switchIfEmpty(Mono.defer(() -> Mono.error(new BadCredentialsException("Token has expired"))))
              .thenReturn(authToken)
              .map(jwtService::getAllClaimsFromToken)
              .map(this::getToken);
      // spotless:on
    }

    private UsernamePasswordAuthenticationToken getToken(Claims claims) {
      String role = claims.get("role", String.class);
      return UsernamePasswordAuthenticationToken.authenticated(
          claims.getSubject(),
          null,
          Stream.of(role).map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }
  }

  @RequiredArgsConstructor
  static class SecurityContextRepository implements ServerSecurityContextRepository {
    private final ReactiveAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
      return Mono.error(new UnsupportedOperationException("Not supported yet."));
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
      return Mono.justOrEmpty(
              exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
          .filter(authHeader -> authHeader.startsWith("Bearer "))
          .switchIfEmpty(Mono.error(() -> new BadCredentialsException("Bearer token is missing")))
          .flatMap(this::authenticate);
    }

    private Mono<SecurityContextImpl> authenticate(String authHeader) {
      String authToken = authHeader.substring(7);
      Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
      return authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
    }
  }
}
