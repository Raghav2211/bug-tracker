package com.github.devraghav.bugtracker.user.security;

import io.jsonwebtoken.Claims;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@ConditionalOnClass({EnableWebFluxSecurity.class, WebFilterChainProxy.class})
@ConditionalOnMissingBean({SecurityWebFilterChain.class, WebFilterChainProxy.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@AutoConfigureBefore(ReactiveSecurityAutoConfiguration.class)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

  private final JWTService jwtService;
  // TODO: pathMatchers :-> version regex

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    // spotless:off
    return http.authorizeExchange()
        .pathMatchers(
                "/api/rest/v1/user/login",
                "/api/rest/v1/user/signup",
                "/favicon.ico" ,
                "/actuator/**")
        .permitAll()
        .pathMatchers(HttpMethod.GET,"/api/rest/v1/user")
        .hasAuthority("ROLE_ADMIN")
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
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
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
          .thenReturn(authToken)
          .switchIfEmpty(Mono.defer(() ->Mono.error(new BadCredentialsException("Invalid token or token has expired"))))
          .map(jwtService::getAllClaimsFromToken)
          .map(this::getToken);
      // spotless:on
    }

    private UsernamePasswordAuthenticationToken getToken(Claims claims) {
      String role = claims.get("role", String.class);
      return new UsernamePasswordAuthenticationToken(
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
