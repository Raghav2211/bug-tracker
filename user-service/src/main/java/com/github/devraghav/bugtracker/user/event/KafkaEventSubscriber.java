package com.github.devraghav.bugtracker.user.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class KafkaEventSubscriber extends Subscriber<SpecificRecordBase> {
  private final String eventStoreTopic;
  private final ReactiveKafkaProducerTemplate<String, SpecificRecordBase>
      reactiveKafkaProducerTemplate;

  public KafkaEventSubscriber(
      ReactiveMessageBroker<SpecificRecordBase> reactiveMessageBroker,
      @Value("${app.kafka.outbound.event_store.topic}") String eventStoreTopic,
      ReactiveKafkaProducerTemplate<String, SpecificRecordBase> reactiveKafkaProducerTemplate) {
    super(reactiveMessageBroker);
    this.eventStoreTopic = eventStoreTopic;
    this.reactiveKafkaProducerTemplate = reactiveKafkaProducerTemplate;
    reactiveKafkaProducerTemplate.partitionsFromProducerFor(eventStoreTopic);
  }

  @Override
  void subscribe(Flux<SpecificRecordBase> stream) {
    stream
        .flatMap(record -> reactiveKafkaProducerTemplate.send(eventStoreTopic, record))
        .subscribe(
            senderResult ->
                log.info(
                    "successfully sent message offset/partition: {}/{}",
                    senderResult.recordMetadata().offset(),
                    senderResult.recordMetadata().partition()),
            exception -> log.error("Exception occurred {}", exception.getMessage(), exception));
  }
}
