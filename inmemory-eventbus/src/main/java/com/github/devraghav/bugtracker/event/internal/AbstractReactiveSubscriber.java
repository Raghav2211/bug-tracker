package com.github.devraghav.bugtracker.event.internal;

public abstract class AbstractReactiveSubscriber<T extends DomainEvent>
    implements EventBus.ReactiveSubscriber<T> {

  public AbstractReactiveSubscriber(
      EventBus.ReactiveMessageBroker reactiveMessageBroker, Class<T> eventClazz) {
    reactiveMessageBroker.subscribe(this, eventClazz);
  }
}
