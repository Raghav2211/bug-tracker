package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.event.Publisher;
import com.github.devraghav.bugtracker.user.event.ReactiveMessageBroker;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class DomainEventPublisher implements Publisher<DomainEvent> {
  private final Sinks.Many<DomainEvent> channel;

  public DomainEventPublisher(ReactiveMessageBroker<DomainEvent> reactiveMessageBroker) {
    this.channel = reactiveMessageBroker.getWriteChannel();
  }

  @Override
  public Mono<Void> publish(DomainEvent message) {
    return Mono.fromRunnable(() -> channel.tryEmitNext(message));
  }
}
