package com.github.devraghav.bugtracker.user.event;

import com.github.devraghav.bugtracker.user.event.internal.DomainEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class DomainEventReactivePublisher implements ReactivePublisher<DomainEvent> {
  private final Sinks.Many<DomainEvent> channel;

  public DomainEventReactivePublisher(ReactiveMessageBroker<DomainEvent> reactiveMessageBroker) {
    this.channel = reactiveMessageBroker.getWriteChannel();
  }

  @Override
  public Mono<Void> publish(DomainEvent message) {
    return Mono.fromRunnable(() -> channel.tryEmitNext(message));
  }
}
