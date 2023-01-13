package com.github.devraghav.bugtracker.event.internal;

import reactor.core.publisher.Sinks;

public class ReactiveChannel<T extends DomainEvent> {

  private final Sinks.Many<DomainEvent> reactiveChannel;

  public ReactiveChannel(Sinks.Many<DomainEvent> reactiveChannel) {
    this.reactiveChannel = reactiveChannel;
  }

  public void tryEmitNext(T domainEvent) {
    reactiveChannel.tryEmitNext(domainEvent);
  }
}
