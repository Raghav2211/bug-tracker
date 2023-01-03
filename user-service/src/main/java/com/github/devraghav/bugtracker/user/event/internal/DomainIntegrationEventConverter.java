package com.github.devraghav.bugtracker.user.event.internal;

import java.util.function.Function;
import org.apache.avro.specific.SpecificRecordBase;

@FunctionalInterface
public interface DomainIntegrationEventConverter<T, R extends SpecificRecordBase> {
  Function<T, R> domainToIntegrationFunc();
}
