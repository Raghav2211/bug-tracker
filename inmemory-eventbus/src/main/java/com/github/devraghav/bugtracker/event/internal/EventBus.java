package com.github.devraghav.bugtracker.event.internal;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public interface EventBus {

  interface ReactivePublisher<T extends DomainEvent> {
    void publish(T message);
  }

  interface ReactiveSubscriber<T extends DomainEvent> {
    void subscribe(EventBus.Subscription<T> subscription);
  }

  interface ReactiveMessageBroker {

    <T extends DomainEvent> WriteChannel<T> register(
        ReactivePublisher<T> publisher, Class<T> domainEventsClass);

    <T extends DomainEvent> void subscribe(
        ReactiveSubscriber<T> subscriber, Class<T> domainEventClass);

    <T extends DomainEvent> EventBus.Subscription<T> tap(
        Supplier<UUID> anonymousSubscriber, Class<T> event);
  }

  @Slf4j
  class WriteChannel<T extends DomainEvent> {

    private final Sinks.Many<DomainEvent> reactiveChannel;

    public WriteChannel(
        String publisher, Class<T> registerOnEvent, Sinks.Many<DomainEvent> reactiveChannel) {
      this.reactiveChannel = reactiveChannel;
      log.atInfo().log(
          "reactive publisher {} has been registered for {}", publisher, registerOnEvent.getName());
    }

    public void publish(T domainEvent) {
      reactiveChannel.tryEmitNext(domainEvent);
    }
  }

  @Slf4j
  class Subscription<T extends DomainEvent> {
    private final Sinks.Many<DomainEvent> reactiveChannel;
    private final Class<T> subscribeOnEvent;
    private final String subscriber;

    public <S extends ReactiveSubscriber<T>> Subscription(
        @NonNull String subscriber,
        @NonNull Class<T> subscribeOnEvent,
        @NonNull Sinks.Many<DomainEvent> reactiveChannel) {
      this.reactiveChannel = reactiveChannel;
      this.subscriber = subscriber;
      this.subscribeOnEvent = subscribeOnEvent;
    }

    public Flux<T> stream() {
      Predicate<DomainEvent> isEventEqual = event -> event.getClass().equals(subscribeOnEvent);
      Predicate<DomainEvent> canAssignUpStreamEvent =
          event -> subscribeOnEvent.isAssignableFrom(event.getClass());

      Consumer<org.reactivestreams.Subscription> onSubscription =
          subscription -> {
            log.atInfo().log("subscriber {} subscribes {}", subscriber, subscribeOnEvent.getName());
          };

      Runnable onCancel =
          () ->
              log.atInfo().log(
                  "subscriber {} cancel subscription for {}",
                  subscriber,
                  subscribeOnEvent.getName());

      Consumer<Throwable> onError =
          (Throwable throwable) ->
              log.error(
                  "error occurred on subscription {} for event {} ",
                  subscriber,
                  subscribeOnEvent.getName());
      return reactiveChannel
          .asFlux()
          .doOnSubscribe(onSubscription)
          .doOnCancel(onCancel)
          .doOnError(onError)
          .filter(isEventEqual.or(canAssignUpStreamEvent))
          .cast(subscribeOnEvent);
    }
  }
}
