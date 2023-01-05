package com.github.devraghav.bugtracker.user.event.internal;

import org.apache.avro.specific.SpecificRecordBase;

@FunctionalInterface
public interface EventConverter<T extends DomainEvent, R extends SpecificRecordBase> {
  R convert(T domainEvent);
}
