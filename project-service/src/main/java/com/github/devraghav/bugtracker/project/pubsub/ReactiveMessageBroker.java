package com.github.devraghav.bugtracker.project.pubsub;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public interface ReactiveMessageBroker<T> {

  Sinks.Many<T> getWriteChannel();

  default Flux<T> getStream() {
    return getWriteChannel().asFlux();
  }
}
