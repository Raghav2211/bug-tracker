package com.github.devraghav.bugtracker.issue.event;

import com.github.devraghav.bugtracker.event.internal.DomainEvent;
import com.github.devraghav.bugtracker.event.internal.EventBus;
import com.github.devraghav.bugtracker.issue.event.internal.IssueEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Component
@Slf4j
public class DomainEventProcessor
    implements EventBus.ReactivePublisher<DomainEvent>, EventBus.ReactiveSubscriber<DomainEvent> {
  private final EventBus.InputChannel<DomainEvent> channel;
  private final String eventStoreTopic;
  private final EventConverterFactory eventConverterFactory;
  private final ReactiveKafkaProducerTemplate<String, SpecificRecordBase>
      reactiveKafkaProducerTemplate;

  public DomainEventProcessor(
      EventBus.ReactiveMessageBroker reactiveMessageBroker,
      @Value("${app.kafka.outbound.event_store.topic}") String eventStoreTopic,
      EventConverterFactory eventConverterFactory,
      ReactiveKafkaProducerTemplate<String, SpecificRecordBase> reactiveKafkaProducerTemplate) {
    channel = reactiveMessageBroker.register(this, DomainEvent.class);
    reactiveMessageBroker.subscribe(this, DomainEvent.class);
    this.eventStoreTopic = eventStoreTopic;
    this.eventConverterFactory = eventConverterFactory;
    this.reactiveKafkaProducerTemplate = reactiveKafkaProducerTemplate;
    //    log.atInfo().log(
    //        "topic {} metadata {}",
    //        eventStoreTopic,
    //        reactiveKafkaProducerTemplate
    //            .partitionsFromProducerFor(eventStoreTopic)
    //            .onErrorResume(KafkaException.class, exception -> Mono.empty())
    //            .blockFirst(Duration.ofMillis(2000)));
  }

  @Override
  public void publish(DomainEvent domainEvent) {
    channel.publish(domainEvent);
  }

  @Override
  public void subscribe(EventBus.OutputChannel<DomainEvent> outputChannel) {
    outputChannel.stream()
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
      case IssueEvent.Created event -> eventConverterFactory
          .getConverter(IssueEvent.Created.class)
          .convert(event);
      case IssueEvent.Updated event -> eventConverterFactory
          .getConverter(IssueEvent.Updated.class)
          .convert(event);
      case IssueEvent.Assigned event -> eventConverterFactory
          .getConverter(IssueEvent.Assigned.class)
          .convert(event);
      case IssueEvent.Resolved event -> eventConverterFactory
          .getConverter(IssueEvent.Resolved.class)
          .convert(event);
      case IssueEvent.Unassigned event -> eventConverterFactory
          .getConverter(IssueEvent.Unassigned.class)
          .convert(event);
      case IssueEvent.WatchStarted event -> eventConverterFactory
          .getConverter(IssueEvent.WatchStarted.class)
          .convert(event);
      case IssueEvent.WatchEnded event -> eventConverterFactory
          .getConverter(IssueEvent.WatchEnded.class)
          .convert(event);
      case IssueEvent.CommentAdded event -> eventConverterFactory
          .getConverter(IssueEvent.CommentAdded.class)
          .convert(event);
      case IssueEvent.CommentUpdated event -> eventConverterFactory
          .getConverter(IssueEvent.CommentUpdated.class)
          .convert(event);
      default -> throw new IllegalArgumentException(
          String.format("No handler found for %s", domainEvent.getName()));
    };
  }
}
