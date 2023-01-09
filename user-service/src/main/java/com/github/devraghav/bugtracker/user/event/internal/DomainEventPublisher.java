package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.pubsub.ReactiveMessageBroker;
import com.github.devraghav.bugtracker.user.pubsub.ReactivePublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class DomainEventPublisher implements ReactivePublisher<DomainEvent> {
  private final Sinks.Many<DomainEvent> channel;

  public DomainEventPublisher(ReactiveMessageBroker<DomainEvent> reactiveMessageBroker) {
    this.channel = reactiveMessageBroker.getWriteChannel();
  }

  @Override
  public Mono<Void> publish(DomainEvent message) {
    return Mono.fromRunnable(() -> channel.tryEmitNext(message));
  }
}
