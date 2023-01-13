package com.github.devraghav.bugtracker.event.internal;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

@Slf4j
public class DefaultReactiveMessageBroker implements EventBus.ReactiveMessageBroker {
  private final Map<Class<? extends DomainEvent>, Sinks.Many<DomainEvent>> streamChannelMap =
      new ConcurrentHashMap<>();

  @Override
  public <T extends DomainEvent> ReactiveChannel<T> register(
      EventBus.ReactivePublisher publisher, Class<T> domainEventsClass) {
    var channel = register(domainEventsClass);
    log.atInfo().log(
        "reactive publisher {} has been registered for {}",
        publisher.getClass().getName(),
        domainEventsClass.getName());
    return new ReactiveChannel<T>(channel);
  }

  @Override
  public <T extends DomainEvent> void subscribe(
      EventBus.ReactiveSubscriber<T> subscriber, Class<T> domainEventClass) {
    var stream =
        getStream(subscriber.getClass().getName(), domainEventClass, register(domainEventClass));
    subscriber.subscribe(stream);
  }

  @Override
  public <T extends DomainEvent> Flux<T> tap(
      Supplier<UUID> anonymousSubscriber, Class<T> domainEventClass) {
    var firstRegisterChannel =
        getFirstRegisterChannel(domainEventClass).orElseGet(() -> DomainEvent.class);

    var channel = streamChannelMap.get(firstRegisterChannel);
    if (channel == null) {
      throw new EventBusException(
          String.format("No channel found for event[%s]", domainEventClass));
    }
    return getStream(anonymousSubscriber.get().toString(), domainEventClass, channel);
  }

  private <T extends DomainEvent> Flux<T> getStream(
      String subscriber, Class<T> domainEventClass, Sinks.Many<DomainEvent> channel) {
    Consumer<Subscription> onSubscription =
        subscription -> {
          log.atInfo().log(
              "subscriber {} subscribe for {}", subscriber, domainEventClass.getName());
        };
    Runnable onComplete = () -> log.atInfo().log("subscriber {} cancel subscription", subscriber);
    return channel
        .asFlux()
        .doOnSubscribe(onSubscription)
        .doOnComplete(onComplete)
        .cast(domainEventClass);
  }

  private <T extends DomainEvent> Sinks.Many<DomainEvent> register(Class<T> domainEventClass) {
    return streamChannelMap.compute(
        domainEventClass,
        (clazz, reactiveChannel) ->
            reactiveChannel == null
                ? Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false)
                : reactiveChannel);
  }

  private <T extends DomainEvent> Optional<Class<? super T>> getFirstRegisterChannel(
      Class<T> clazz) {
    Class<? super T> superclass = clazz.getSuperclass();
    while (superclass != null) {
      Class<? super T> superclazz = superclass;
      superclass = superclazz.getSuperclass();
      if (superclass == Object.class) break;
      return Optional.of(superclass);
    }
    return Optional.empty();
  }
}
