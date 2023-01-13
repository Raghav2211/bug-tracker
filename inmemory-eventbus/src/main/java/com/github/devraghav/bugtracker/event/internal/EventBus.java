package com.github.devraghav.bugtracker.event.internal;

import java.util.UUID;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public interface EventBus {

  interface ReactivePublisher<T extends DomainEvent> {
    Mono<Void> publish(T message);
  }

  interface ReactiveSubscriber<T extends DomainEvent> {
    void subscribe(Flux<T> stream);
  }

  interface ReactiveMessageBroker {

    <T extends DomainEvent> WriteChannel<T> register(
        ReactivePublisher<T> publisher, Class<T> domainEventsClass);

    <T extends DomainEvent> void subscribe(
        ReactiveSubscriber<T> subscriber, Class<T> domainEventClass);

    <T extends DomainEvent> Flux<T> tap(Supplier<UUID> anonymousSubscriber, Class<T> event);
  }

  class WriteChannel<T extends DomainEvent> {

    private final Sinks.Many<DomainEvent> reactiveChannel;

    public WriteChannel(Sinks.Many<DomainEvent> reactiveChannel) {
      this.reactiveChannel = reactiveChannel;
    }

    public void publish(T domainEvent) {
      reactiveChannel.tryEmitNext(domainEvent);
    }
  }
}
