package com.github.devraghav.bugtracker.user.pubsub;

import reactor.core.publisher.Mono;

public interface ReactivePublisher<T> {
  Mono<Void> publish(T message);
}
