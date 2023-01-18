package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.LoginRequest;
import com.github.devraghav.bugtracker.user.dto.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LoginService {
  private final JWTService jwtService;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  public Mono<LoginRequest.Response> login(LoginRequest.Request request) {
    return userService
        .findByEmail(request.email())
        .filter(user -> passwordEncoder.matches(request.password(), user.password()))
        .map(jwtService::generateToken)
        .map(LoginRequest.Response::new)
        .switchIfEmpty(Mono.error(() -> UserException.unauthorizedAccess(request.email())));
  }
}
