package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.ReactiveMessageBroker;
import reactor.core.publisher.Flux;

public abstract class ReactiveSubscriber<T> {

  public ReactiveSubscriber(ReactiveMessageBroker<T> reactiveMessageBroker) {
    subscribe(reactiveMessageBroker.getStream());
  }

  protected abstract void subscribe(Flux<T> stream);
}
