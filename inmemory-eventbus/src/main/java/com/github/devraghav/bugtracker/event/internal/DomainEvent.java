package com.github.devraghav.bugtracker.event.internal;

import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.Getter;

@Getter
public abstract class DomainEvent {
  private final UUID id = UUID.randomUUID();
  private final String aggregateId;
  private final LocalDateTime logTime = LocalDateTime.now();
  private final String name;
  private final String publisher;
  private final String requestedBy;

  public record PublisherInfo(String serviceName, Class<?> domainClazz, String requestedBy) {}

  public DomainEvent(String aggregateId, String action, PublisherInfo publisherInfo) {
    this.aggregateId = aggregateId;
    name =
        new StringJoiner("#")
            .add(publisherInfo.serviceName())
            .add(this.getClass().getSimpleName())
            .add(action)
            .toString();
    this.publisher =
        new StringJoiner("#").add("Service").add(publisherInfo.serviceName()).toString();
    this.requestedBy = publisherInfo.requestedBy();
  }
}
