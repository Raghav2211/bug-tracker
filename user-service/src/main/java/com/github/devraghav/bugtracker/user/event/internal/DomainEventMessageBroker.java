package com.github.devraghav.bugtracker.user.event.internal;

import com.github.devraghav.bugtracker.user.pubsub.ReactiveMessageBroker;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
public class DomainEventMessageBroker implements ReactiveMessageBroker<DomainEvent> {
  private Sinks.Many<DomainEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

  @Override
  public Sinks.Many<DomainEvent> getWriteChannel() {
    return sink;
  }
}
