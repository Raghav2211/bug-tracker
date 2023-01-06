package com.github.devraghav.bugtracker.project.event.internal;

import reactor.core.publisher.Mono;

public interface ReactivePublisher<T> {
  Mono<Void> publish(T message);
}
