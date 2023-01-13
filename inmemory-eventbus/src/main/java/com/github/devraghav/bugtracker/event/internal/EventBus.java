package com.github.devraghav.bugtracker.event.internal;

import java.util.UUID;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EventBus {

  public interface ReactivePublisher {
    Mono<Void> publish(DomainEvent message);
  }

  public interface ReactiveSubscriber<T extends DomainEvent> {
    void subscribe(Flux<T> stream);
  }

  public interface ReactiveMessageBroker {

    <T extends DomainEvent> ReactiveChannel<T> register(
        ReactivePublisher publisher, Class<T> domainEventsClass);

    <T extends DomainEvent> void subscribe(
        ReactiveSubscriber<T> subscriber, Class<T> domainEventClass);

    // Anyone can tap the current stream for any event(s) at any point of time
    <T extends DomainEvent> Flux<T> tap(Supplier<UUID> anonymousSubscriber, Class<T> event);
  }
}
