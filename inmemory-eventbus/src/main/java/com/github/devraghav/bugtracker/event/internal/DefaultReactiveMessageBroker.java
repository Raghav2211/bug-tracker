package com.github.devraghav.bugtracker.event.internal;

import java.util.*;
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
  public <T extends DomainEvent> EventBus.WriteChannel<T> register(
      EventBus.ReactivePublisher<T> publisher, Class<T> domainEventsClass) {
    var channel = register(domainEventsClass);
    log.atInfo().log(
        "reactive publisher {} has been registered for {}",
        publisher.getClass().getName(),
        domainEventsClass.getName());
    return new EventBus.WriteChannel<T>(channel);
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
    var reactiveChannel =
        getAllRegisteredParentEvents(domainEventClass).stream()
            .map(streamChannelMap::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> EventBusException.channelNotFound(domainEventClass));
    return getStream(anonymousSubscriber.get().toString(), domainEventClass, reactiveChannel);
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
    Supplier<Sinks.Many<DomainEvent>> registerNewEventSupplier =
        () ->
            streamChannelMap.compute(
                domainEventClass,
                (clazz, reactiveChannel) ->
                    Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false));
    var parentEvents = getAllRegisteredParentEvents(domainEventClass);
    return parentEvents.stream()
        .map(streamChannelMap::get)
        .filter(Objects::nonNull)
        .findFirst()
        .orElseGet(registerNewEventSupplier::get);
  }

  private <T extends DomainEvent> Collection<Class<? super T>> getAllRegisteredParentEvents(
      Class<T> clazz) {
    var channels = new ArrayList<Class<? super T>>();
    channels.add(clazz); // self
    if (clazz == DomainEvent.class) {
      return channels;
    }
    var superclass = clazz.getSuperclass();
    channels.add(superclass);
    while (superclass != null) {
      var superclazz = superclass;
      superclass = superclazz.getSuperclass();
      channels.add(superclass);
      if (superclass == DomainEvent.class) {
        break;
      }
    }
    return channels;
  }
}
