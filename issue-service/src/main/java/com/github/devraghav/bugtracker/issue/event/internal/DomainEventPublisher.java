package com.github.devraghav.bugtracker.issue.event.internal;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher implements EventBus.ReactivePublisher<DomainEvent> {
  private final EventBus.WriteChannel<DomainEvent> channel;

  public DomainEventPublisher(EventBus.ReactiveMessageBroker reactiveMessageBroker) {
    this.channel = reactiveMessageBroker.register(this, DomainEvent.class);
  }

  @Override
  public void publish(DomainEvent message) {
    channel.publish(message);
  }
}
