package com.github.devraghav.bugtracker.project.event.internal;

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

  record PublisherInfo(String name, Class<?> domain) {}

  public DomainEvent(String action, PublisherInfo publisherInfo) {
    name =
        new StringJoiner("#")
            .add(publisherInfo.name())
            .add(publisherInfo.domain().getSimpleName())
            .add(action)
            .toString();
    this.publisher = new StringJoiner("#").add("Service").add(publisherInfo.name()).toString();
  }
}
