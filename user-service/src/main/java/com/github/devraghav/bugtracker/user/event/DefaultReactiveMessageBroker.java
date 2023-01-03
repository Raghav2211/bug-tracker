package com.github.devraghav.bugtracker.user.event;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
public class DefaultReactiveMessageBroker implements ReactiveMessageBroker<SpecificRecordBase> {
  private Sinks.Many<SpecificRecordBase> sink = Sinks.many().multicast().onBackpressureBuffer();

  @Override
  public Sinks.Many<SpecificRecordBase> getWriteChannel() {
    return sink;
  }
}
