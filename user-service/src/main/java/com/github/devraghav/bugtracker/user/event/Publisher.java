package com.github.devraghav.bugtracker.user.event;

import reactor.core.publisher.Mono;

public interface Publisher<T> {
  Mono<Void> publish(T message);
}
