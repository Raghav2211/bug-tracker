package com.github.devraghav.bugtracker.user.kafka.producer;

import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.github.devraghav.data_model.command.user.UserCreateCommand;
import com.github.devraghav.data_model.domain.user.NewUser;
import com.github.devraghav.data_model.event.user.UserCreatedEvent;
import com.github.devraghav.data_model.event.user.UserDuplicatedEvent;
import com.github.devraghav.data_model.schema.user.UserCreateCommandSchema;
import com.github.devraghav.data_model.schema.user.UserCreatedEventSchema;
import com.github.devraghav.data_model.schema.user.UserDuplicatedEventSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Component
@Slf4j
public record KafkaProducer(
    String eventStoreTopic,
    ReactiveKafkaProducerTemplate<String, SpecificRecordBase> reactiveKafkaProducerTemplate) {
  public KafkaProducer(
      @Value("${app.kafka.outbound.event_store.topic}") String eventStoreTopic,
      ReactiveKafkaProducerTemplate<String, SpecificRecordBase> reactiveKafkaProducerTemplate) {
    this.eventStoreTopic = eventStoreTopic;
    this.reactiveKafkaProducerTemplate = reactiveKafkaProducerTemplate;
  }

  private <T extends SpecificRecordBase> Mono<SenderResult<Void>> send(T record) {
    return reactiveKafkaProducerTemplate
        .send(eventStoreTopic, record)
        .doOnSuccess(
            senderResult ->
                log.info("sent {} offset : {}", record, senderResult.recordMetadata().offset()));
  }

  public Mono<UserRequest> generateAndSendUserCreateCommand(
      String requestId, UserRequest userRequest) {
    var command = getUserCreateCommandSchema(requestId, userRequest);
    log.atDebug().log("User create command {}", command);
    return send(command).thenReturn(userRequest);
  }

  public Mono<UserRequest> generateAndSendUserDuplicatedEvent(
      String requestId, UserRequest userRequest) {
    var event = getUserDuplicatedEventSchema(requestId, userRequest);
    log.atDebug().log("User created event {}", event);
    return send(event).thenReturn(userRequest);
  }

  public Mono<User> generateAndSendUserCreatedEvent(String requestId, User user) {
    var event = getUserCreatedEventSchema(requestId, user);
    log.atDebug().log("User created event {}", event);
    return send(event).thenReturn(user);
  }

  private UserCreateCommandSchema getUserCreateCommandSchema(
      String requestId, UserRequest userRequest) {
    return UserCreateCommandSchema.newBuilder()
        .setCommand(
            UserCreateCommand.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("User.User.Create")
                .setPayload(getNewUSer(userRequest))
                .setPublisher("Service.User")
                .build())
        .build();
  }

  private UserDuplicatedEventSchema getUserDuplicatedEventSchema(
      String requestId, UserRequest userRequest) {
    return UserDuplicatedEventSchema.newBuilder()
        .setEvent(
            UserDuplicatedEvent.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("User.User.Duplicated")
                .setPayload(getNewUSer(userRequest))
                .setPublisher("Service.User")
                .build())
        .build();
  }

  private UserCreatedEventSchema getUserCreatedEventSchema(String requestId, User user) {
    return UserCreatedEventSchema.newBuilder()
        .setEvent(
            UserCreatedEvent.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("User.User.Created")
                .setPayload(getUser(user))
                .setPublisher("Service.User")
                .build())
        .build();
  }

  private NewUser getNewUSer(UserRequest userRequest) {
    return NewUser.newBuilder()
        .setAccessLevel(userRequest.access().name())
        .setEmail(userRequest.email())
        .setFirstName(userRequest.firstName())
        .setLastName(userRequest.lastName())
        .build();
  }

  private com.github.devraghav.data_model.domain.user.User getUser(User user) {
    return com.github.devraghav.data_model.domain.user.User.newBuilder()
        .setId(user.id())
        .setAccessLevel(user.access().name())
        .setEmail(user.email())
        .setEnabled(user.enabled())
        .setFirstName(user.firstName())
        .setLastName(user.lastName())
        .build();
  }
}
