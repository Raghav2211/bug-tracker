package com.github.devraghav.bugtracker.user.pubsub;

import reactor.core.publisher.Flux;

public abstract class ReactiveSubscriber<T> {

  public ReactiveSubscriber(ReactiveMessageBroker<T> reactiveMessageBroker) {
    subscribe(reactiveMessageBroker.getStream());
  }

  protected abstract void subscribe(Flux<T> stream);
}
