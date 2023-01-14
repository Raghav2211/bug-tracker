package com.github.devraghav.bugtracker.project.event;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import org.apache.avro.specific.SpecificRecordBase;

@FunctionalInterface
public interface EventConverter<T extends DomainEvent, R extends SpecificRecordBase> {
  R convert(T domainEvent);
}
