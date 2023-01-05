package com.github.devraghav.bugtracker.user.event;

import reactor.core.publisher.Mono;

public interface ReactivePublisher<T> {
  Mono<Void> publish(T message);
}
