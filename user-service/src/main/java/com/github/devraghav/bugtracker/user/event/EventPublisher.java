package com.github.devraghav.bugtracker.user.event;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class EventPublisher implements Publisher<SpecificRecordBase> {

  private final Sinks.Many<SpecificRecordBase> channel;

  public EventPublisher(ReactiveMessageBroker<SpecificRecordBase> reactiveMessageBroker) {
    this.channel = reactiveMessageBroker.getWriteChannel();
  }

  @Override
  public void publish(SpecificRecordBase message) {
    channel.tryEmitNext(message);
  }
}
