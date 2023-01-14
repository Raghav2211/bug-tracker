package com.github.devraghav.bugtracker.event.internal;

import java.util.Map;
import lombok.Getter;

@Getter
public class EventBusException extends RuntimeException {
  private final Map<String, Object> meta;

  private EventBusException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  public static EventBusException channelNotFound(Class<? extends DomainEvent> domainEventClazz) {
    return new EventBusException(
        "No reactiveChannel found for event", Map.of("event-class", domainEventClazz.getName()));
  }
}
