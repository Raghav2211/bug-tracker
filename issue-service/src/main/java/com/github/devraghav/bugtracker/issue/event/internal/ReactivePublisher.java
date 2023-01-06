package com.github.devraghav.bugtracker.issue.event.internal;

import reactor.core.publisher.Mono;

public interface ReactivePublisher<T> {
  Mono<Void> publish(T message);
}
