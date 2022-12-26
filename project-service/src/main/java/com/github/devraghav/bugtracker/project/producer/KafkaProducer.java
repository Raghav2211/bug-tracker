package com.github.devraghav.bugtracker.project.producer;

import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import com.github.devraghav.bugtracker.project.dto.ProjectVersion;
import com.github.devraghav.data_model.command.project.ProjectCreateCommand;
import com.github.devraghav.data_model.domain.project.NewProject;
import com.github.devraghav.data_model.domain.project.Version;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.project.ProjectCreatedEvent;
import com.github.devraghav.data_model.event.project.ProjectDuplicatedEvent;
import com.github.devraghav.data_model.schema.project.ProjectCreateCommandSchema;
import com.github.devraghav.data_model.schema.project.ProjectCreatedEventSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.devraghav.data_model.schema.project.ProjectDuplicatedEventSchema;
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

  public Mono<ProjectRequest> generateAndSendProjectCreateCommand(
      String requestId, ProjectRequest projectRequest) {
    var command = getProjectCreateCommandSchema(requestId, projectRequest);
    log.atDebug().log("Project create command {}", command);
    return send(command).thenReturn(projectRequest);
  }

  public Mono<ProjectRequest> generateAndSendProjectDuplicatedEvent(
      String requestId, ProjectRequest projectRequest) {
    var event = getProjectDuplicatedEventSchema(requestId, projectRequest);
    log.atDebug().log("Project created event {}", event);
    return send(event).thenReturn(projectRequest);
  }

  public Mono<Project> generateAndSendProjectCreatedEvent(String requestId, Project project) {
    var event = getProjectCreatedEventSchema(requestId, project);
    log.atDebug().log("Project created event {}", event);
    return send(event).thenReturn(project);
  }

  private ProjectCreateCommandSchema getProjectCreateCommandSchema(
      String requestId, ProjectRequest projectRequest) {
    return ProjectCreateCommandSchema.newBuilder()
        .setCommand(
            ProjectCreateCommand.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Project.Create")
                .setPayload(getNewProject(projectRequest))
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private ProjectDuplicatedEventSchema getProjectDuplicatedEventSchema(
      String requestId, ProjectRequest projectRequest) {
    return ProjectDuplicatedEventSchema.newBuilder()
        .setEvent(
            ProjectDuplicatedEvent.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Project.Duplicated")
                .setPayload(getNewProject(projectRequest))
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private ProjectCreatedEventSchema getProjectCreatedEventSchema(
      String requestId, Project project) {
    return ProjectCreatedEventSchema.newBuilder()
        .setEvent(
            ProjectCreatedEvent.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Project.Created")
                .setPayload(getProject(project))
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private NewProject getNewProject(
      ProjectRequest projectRequest) {
    var tags =
        projectRequest.tags().entrySet().stream()
            .collect(Collectors.toMap(Objects::toString, Object::toString));
    return NewProject.newBuilder()
        .setName(projectRequest.name())
        .setAuthorId(projectRequest.author())
        .setDescription(projectRequest.description())
        .setStatus(projectRequest.status().name())
        .setTags(tags)
        .build();
  }

  private com.github.devraghav.data_model.domain.project.Project getProject(Project project) {
    var tags =
        project.getTags().entrySet().stream()
            .collect(Collectors.toMap(Objects::toString, Object::toString));
    return com.github.devraghav.data_model.domain.project.Project.newBuilder()
        .setId(project.getId())
        .setName(project.getName())
        .setAuthor(getUser(project.getAuthor()))
        .setDescription(project.getDescription())
        .setEnabled(project.getEnabled())
        .setCreatedAt(project.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
        .setTags(tags)
        .setStatus(project.getStatus().name())
        .setVersions(getVersions(project.getVersions()))
        .build();
  }

  private User getUser(com.github.devraghav.bugtracker.project.dto.User author) {
    return User.newBuilder()
        .setId(author.id())
        .setAccessLevel(author.access().name())
        .setEmail(author.email())
        .setEnabled(author.enabled())
        .setFirstName(author.firstName())
        .setLastName(author.lastName())
        .build();
  }

  private List<Version> getVersions(Set<ProjectVersion> versions) {
    return versions.stream().map(this::getVersion).collect(Collectors.toList());
  }

  private Version getVersion(ProjectVersion version) {
    return Version.newBuilder().setId(version.id()).setVersion(version.version()).build();
  }
}
