package com.github.devraghav.bugtracker.user.event.internal;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.Getter;

@Getter
public abstract class DomainEvent {
  private final UUID id = UUID.randomUUID();
  private final LocalDateTime logTime = LocalDateTime.now();
  private final String name;
  private final String publisher;

  public DomainEvent(String serviceName, String eventType, Class<?> domainClass) {
    name =
        new StringJoiner(".").add(serviceName).add(domainClass.getName()).add(eventType).toString();
    publisher = new StringJoiner(".").add("Service").add(serviceName).toString();
  }
}
