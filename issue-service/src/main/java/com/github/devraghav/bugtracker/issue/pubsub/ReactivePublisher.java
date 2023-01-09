package com.github.devraghav.bugtracker.issue.pubsub;

import reactor.core.publisher.Mono;

public interface ReactivePublisher<T> {
  Mono<Void> publish(T message);
}
