package com.github.devraghav.bugtracker.user.event;

import com.github.devraghav.bugtracker.user.event.internal.*;
import com.github.devraghav.bugtracker.user.pubsub.ReactiveMessageBroker;
import com.github.devraghav.bugtracker.user.pubsub.ReactiveSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Component
@Slf4j
public class IntegrationEventPublisher extends ReactiveSubscriber<DomainEvent> {
  private final String eventStoreTopic;
  private final EventConverterFactory eventConverterFactory;
  private final ReactiveKafkaProducerTemplate<String, SpecificRecordBase>
      reactiveKafkaProducerTemplate;

  public IntegrationEventPublisher(
      ReactiveMessageBroker<DomainEvent> reactiveMessageBroker,
      @Value("${app.kafka.outbound.event_store.topic}") String eventStoreTopic,
      EventConverterFactory eventConverterFactory,
      ReactiveKafkaProducerTemplate<String, SpecificRecordBase> reactiveKafkaProducerTemplate) {
    super(reactiveMessageBroker);
    this.eventStoreTopic = eventStoreTopic;
    this.eventConverterFactory = eventConverterFactory;
    this.reactiveKafkaProducerTemplate = reactiveKafkaProducerTemplate;
    reactiveKafkaProducerTemplate.partitionsFromProducerFor(eventStoreTopic);
  }

  @Override
  protected void subscribe(Flux<DomainEvent> stream) {
    stream
        .map(this::getKeyValue)
        .flatMap(keyValue -> send(keyValue.getKey(), keyValue.getValue()))
        .subscribe(
            senderResult ->
                log.info(
                    "successfully sent message offset/partition: {}/{}",
                    senderResult.recordMetadata().offset(),
                    senderResult.recordMetadata().partition()),
            exception -> log.error("Exception occurred {}", exception.getMessage(), exception));
  }

  private Mono<SenderResult<Void>> send(String key, SpecificRecordBase value) {
    return reactiveKafkaProducerTemplate.send(eventStoreTopic, key, value);
  }

  private Pair<String, SpecificRecordBase> getKeyValue(DomainEvent event) {
    return Pair.of(event.getPublisher(), getAvroRecord(event));
  }

  private SpecificRecordBase getAvroRecord(DomainEvent domainEvent) {
    return switch (domainEvent) {
      case UserCreatedEvent userCreatedEvent -> eventConverterFactory
          .getConverter(UserCreatedEvent.class)
          .convert(userCreatedEvent);
      case UserDuplicatedEvent userDuplicatedEvent -> eventConverterFactory
          .getConverter(UserDuplicatedEvent.class)
          .convert(userDuplicatedEvent);
      default -> throw new IllegalArgumentException(
          String.format("No handler found for %s", domainEvent.getName()));
    };
  }
}
