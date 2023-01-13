package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.event.internal.ReactiveChannel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DomainEventPublisher implements EventBus.ReactivePublisher {
  private final ReactiveChannel<DomainEvent> channel;

  public DomainEventPublisher(EventBus.ReactiveMessageBroker reactiveMessageBroker) {
    this.channel = reactiveMessageBroker.register(this, DomainEvent.class);
  }

  @Override
  public Mono<Void> publish(DomainEvent message) {
    return Mono.fromRunnable(() -> channel.tryEmitNext(message));
  }
}
