package com.github.devraghav.bugtracker.project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
public class SecurityConfig {

  private final JWTService jwtService;
  private final ReactiveAuthenticationManager authenticationManager;
  private final ServerSecurityContextRepository securityContextRepository;

  public SecurityConfig(@Value("${app.jwt.secret}") String secret) {
    this.jwtService = new JWTService(secret);
    authenticationManager = new AuthenticationManager(jwtService);
    securityContextRepository = new SecurityContextRepository(authenticationManager);
  }

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    // spotless:off
    return http.authorizeExchange()
            .pathMatchers("/actuator/**")
            .permitAll()
            .pathMatchers(HttpMethod.POST, "/api/rest/v1/project")
            .hasAnyAuthority(Role.ROLE_ADMIN.name(),Role.ROLE_WRITE.name())
            .pathMatchers(HttpMethod.POST, "/api/rest/v1/project/{id}/version")
            .hasAnyAuthority(Role.ROLE_ADMIN.name(),Role.ROLE_WRITE.name())
            .anyExchange()
            .authenticated()
            .and()
            .formLogin()
            .disable()
            .csrf()
            .disable()
            .httpBasic()
            .disable()
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .exceptionHandling()
            .authenticationEntryPoint((serverWebExchange, authenticationException) -> Mono.fromRunnable(() ->serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
            .accessDeniedHandler((serverWebExchange, authenticationException) -> Mono.fromRunnable(() -> serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
            .and()
            .build();
    // spotless:on
  }

  @RequiredArgsConstructor
  private static class AuthenticationManager implements ReactiveAuthenticationManager {
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
      Role role = Role.valueOf(claims.get("role", String.class));
      Boolean enabled = claims.get("enabled", Boolean.class);
      return UsernamePasswordAuthenticationToken.authenticated(
          claims.getSubject(),
          null,
          Stream.of(role)
              .map(Role::name)
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toList()));
    }
  }

  @RequiredArgsConstructor
  private static class SecurityContextRepository implements ServerSecurityContextRepository {
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

  @Slf4j
  private static class JWTService {

    private final Key key;

    public JWTService(String secret) {
      key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    Claims getAllClaimsFromToken(String token) {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    Date getExpirationDateFromToken(String token) {
      return getAllClaimsFromToken(token).getExpiration();
    }

    Boolean validateToken(String token) {
      boolean isTokenNotExpired = isTokenExpired(token);
      log.atDebug().log("is token expired?  {}", isTokenNotExpired);
      return BooleanUtils.negate(isTokenNotExpired);
    }

    private Boolean isTokenExpired(String token) {
      final Date expiration = getExpirationDateFromToken(token);
      return expiration.before(new Date());
    }
  }

  private enum Role {
    ROLE_ADMIN,
    ROLE_READ,
    ROLE_WRITE;
  }
}
