package com.github.devraghav.bugtracker.user.service;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.event.Publisher;
import com.github.devraghav.bugtracker.user.event.internal.DomainIntegrationEventConverterFactory;
import com.github.devraghav.bugtracker.user.event.internal.UserCreatedEvent;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public record UserAPIEventHandler(
    DomainIntegrationEventConverterFactory domainIntegrationEventConverterFactory,
    Publisher<SpecificRecordBase> publisher) {

  public Mono<Void> handleUserDuplicated(CreateUserRequest createUserRequest) {
    return Mono.fromRunnable(
        () -> {
          var integrationEvent =
              domainIntegrationEventConverterFactory
                  .getConverter(CreateUserRequest.class)
                  .domainToIntegrationFunc()
                  .apply(createUserRequest);
          publisher.publish(integrationEvent);
        });
  }

  public Mono<Void> handleUserCreated(UserCreatedEvent userCreatedEvent) {
    return Mono.fromRunnable(
        () -> {
          var integrationEvent =
              domainIntegrationEventConverterFactory
                  .getConverter(UserCreatedEvent.class)
                  .domainToIntegrationFunc()
                  .apply(userCreatedEvent);
          publisher.publish(integrationEvent);
        });
  }


}
