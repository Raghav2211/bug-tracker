package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.issue.event.internal.DomainEvent;
import org.apache.avro.specific.SpecificRecordBase;

@FunctionalInterface
public interface EventConverter<T extends DomainEvent, R extends SpecificRecordBase> {
  R convert(T domainEvent);
}
