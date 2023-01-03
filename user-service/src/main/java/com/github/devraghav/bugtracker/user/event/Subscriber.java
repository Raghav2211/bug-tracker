package com.github.devraghav.bugtracker.user.event;

import reactor.core.publisher.Flux;

public abstract class Subscriber<T> {

  public Subscriber(ReactiveMessageBroker<T> reactiveMessageBroker) {
    subscribe(reactiveMessageBroker.getStream());
  }

  abstract void subscribe(Flux<T> stream);
}
