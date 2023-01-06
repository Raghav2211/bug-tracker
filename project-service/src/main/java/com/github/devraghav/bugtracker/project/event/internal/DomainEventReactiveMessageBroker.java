package com.github.devraghav.bugtracker.project.event.internal;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
public class DomainEventReactiveMessageBroker implements ReactiveMessageBroker<DomainEvent> {
  private Sinks.Many<DomainEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

  @Override
  public Sinks.Many<DomainEvent> getWriteChannel() {
    return sink;
  }
}
