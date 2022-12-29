package com.github.devraghav.bugtracker.user.kafka.producer;

import com.github.devraghav.bugtracker.user.dto.CreateUserRequest;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.data_model.command.user.CreateUser;
import com.github.devraghav.data_model.domain.user.NewUser;
import com.github.devraghav.data_model.event.user.UserCreated;
import com.github.devraghav.data_model.event.user.UserDuplicated;
import com.github.devraghav.data_model.schema.user.CreateUserSchema;
import com.github.devraghav.data_model.schema.user.UserCreatedSchema;
import com.github.devraghav.data_model.schema.user.UserDuplicatedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
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

  public Mono<CreateUserRequest> sendUserCreateCommand(
      String requestId, CreateUserRequest createUserRequest) {
    var command = getCreateUserSchema(requestId, createUserRequest);
    log.atDebug().log("User create command {}", command);
    return send(command).thenReturn(createUserRequest);
  }

  public Mono<CreateUserRequest> sendUserDuplicatedEvent(
      String requestId, CreateUserRequest createUserRequest) {
    var event = getUserDuplicatedSchema(requestId, createUserRequest);
    log.atDebug().log("User created event {}", event);
    return send(event).thenReturn(createUserRequest);
  }

  public Mono<User> sendUserCreatedEvent(String requestId, User user) {
    var event = getUserCreatedSchema(requestId, user);
    log.atDebug().log("User created event {}", event);
    return send(event).thenReturn(user);
  }

  private CreateUserSchema getCreateUserSchema(
      String requestId, CreateUserRequest createUserRequest) {
    return CreateUserSchema.newBuilder()
        .setCommand(
            CreateUser.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("User.User.Create")
                .setPayload(getNewUSer(createUserRequest))
                .setPublisher("Service.User")
                .build())
        .build();
  }

  private UserDuplicatedSchema getUserDuplicatedSchema(
      String requestId, CreateUserRequest createUserRequest) {
    return UserDuplicatedSchema.newBuilder()
        .setEvent(
            UserDuplicated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("User.User.Duplicated")
                .setPayload(getNewUSer(createUserRequest))
                .setPublisher("Service.User")
                .build())
        .build();
  }

  private UserCreatedSchema getUserCreatedSchema(String requestId, User user) {
    return UserCreatedSchema.newBuilder()
        .setEvent(
            UserCreated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("User.User.Created")
                .setPayload(getUser(user))
                .setPublisher("Service.User")
                .build())
        .build();
  }

  private NewUser getNewUSer(CreateUserRequest createUserRequest) {
    return NewUser.newBuilder()
        .setAccessLevel(createUserRequest.access().name())
        .setEmail(createUserRequest.email())
        .setFirstName(createUserRequest.firstName())
        .setLastName(createUserRequest.lastName())
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
