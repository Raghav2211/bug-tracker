package com.github.devraghav.bugtracker.event.internal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

public class DefaultReactiveMessageBroker implements EventBus.ReactiveMessageBroker {
  private final Map<Class<? extends DomainEvent>, Sinks.Many<DomainEvent>> streamChannelMap =
      new ConcurrentHashMap<>();

  @Override
  public <T extends DomainEvent> EventBus.InputChannel<T> register(
      EventBus.ReactivePublisher<T> publisher, Class<T> registerOnEvent) {
    var channel = register(registerOnEvent);
    return new EventBus.InputChannel<T>(publisher.getClass().getName(), registerOnEvent, channel);
  }

  @Override
  public <T extends DomainEvent> void subscribe(
      EventBus.ReactiveSubscriber<T> subscriber, Class<T> subscribeOnEvent) {
    var reactiveChannel = register(subscribeOnEvent);
    var subscription =
        new EventBus.OutputChannel<>(
            subscriber.getClass().getName(), subscribeOnEvent, reactiveChannel);
    subscriber.subscribe(subscription);
  }

  @Override
  public <T extends DomainEvent> EventBus.OutputChannel<T> tap(
      Supplier<UUID> anonymousSubscriber, Class<T> subscribeOnEvent) {
    var reactiveChannel =
        getAllRegisteredParentEvents(subscribeOnEvent).stream()
            .map(streamChannelMap::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> EventBusException.channelNotFound(subscribeOnEvent));
    return new EventBus.OutputChannel<>(
        anonymousSubscriber.get().toString(), subscribeOnEvent, reactiveChannel);
  }

  private <T extends DomainEvent> Sinks.Many<DomainEvent> register(Class<T> registerOnEvent) {
    Supplier<Sinks.Many<DomainEvent>> registerNewEventSupplier =
        () ->
            streamChannelMap.compute(
                registerOnEvent,
                (clazz, reactiveChannel) ->
                    Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false));
    var parentEvents = getAllRegisteredParentEvents(registerOnEvent);
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
