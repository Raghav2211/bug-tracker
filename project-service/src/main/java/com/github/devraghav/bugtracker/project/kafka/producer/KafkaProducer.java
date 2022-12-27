package com.github.devraghav.bugtracker.project.kafka.producer;

import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import com.github.devraghav.bugtracker.project.dto.ProjectVersion;
import com.github.devraghav.bugtracker.project.dto.ProjectVersionRequest;
import com.github.devraghav.data_model.command.project.CreateProject;
import com.github.devraghav.data_model.command.project.version.CreateVersion;
import com.github.devraghav.data_model.domain.project.NewProject;
import com.github.devraghav.data_model.domain.project.version.NewVersion;
import com.github.devraghav.data_model.domain.project.version.Version;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.project.ProjectCreated;
import com.github.devraghav.data_model.event.project.ProjectDuplicated;
import com.github.devraghav.data_model.event.project.version.VersionCreated;
import com.github.devraghav.data_model.schema.project.*;
import com.github.devraghav.data_model.schema.project.version.CreateVersionSchema;
import com.github.devraghav.data_model.schema.project.version.VersionCreatedSchema;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

  public Mono<ProjectRequest> sendProjectCreateCommand(
      String requestId, ProjectRequest projectRequest) {
    var command = getCreateProjectSchema(requestId, projectRequest);
    log.atDebug().log("CreateProjectSchema {}", command);
    return send(command).thenReturn(projectRequest);
  }

  public Mono<ProjectVersionRequest> sendProjectVersionCreateCommand(
      String requestId, String projectId, ProjectVersionRequest projectVersionRequest) {
    var command = getCreateVersionSchema(requestId, projectId, projectVersionRequest);
    log.atDebug().log("ProjectVersion create command {}", command);
    return send(command).thenReturn(projectVersionRequest);
  }

  public Mono<ProjectRequest> sendProjectDuplicatedEvent(
      String requestId, ProjectRequest projectRequest) {
    var event = getProjectDuplicatedSchema(requestId, projectRequest);
    log.atDebug().log("ProjectDuplicatedSchema {}", event);
    return send(event).thenReturn(projectRequest);
  }

  public Mono<Project> sendProjectCreatedEvent(String requestId, Project project) {
    var event = getProjectCreatedSchema(requestId, project);
    log.atDebug().log("ProjectCreatedSchema {}", event);
    return send(event).thenReturn(project);
  }

  public Mono<ProjectVersion> sendProjectVersionCreatedEvent(
      String requestId, String projectId, ProjectVersion projectVersion) {
    log.atDebug().log(
        "create VersionCreateSchema against requestId {} projectId {} projectVersion {} ",
        requestId,
        projectId,
        projectVersion);
    var event = getVersionCreatedSchema(requestId, projectId, projectVersion);
    log.atDebug().log("VersionCreateSchema {}", event);
    return send(event).thenReturn(projectVersion);
  }

  private CreateProjectSchema getCreateProjectSchema(
      String requestId, ProjectRequest projectRequest) {
    return CreateProjectSchema.newBuilder()
        .setCommand(
            CreateProject.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Project.Create")
                .setPayload(getNewProject(projectRequest))
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private CreateVersionSchema getCreateVersionSchema(
      String requestId, String projectId, ProjectVersionRequest projectVersionRequest) {
    return CreateVersionSchema.newBuilder()
        .setCommand(
            CreateVersion.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Version.Create")
                .setProjectId(projectId)
                .setPayload(
                    NewVersion.newBuilder().setVersion(projectVersionRequest.version()).build())
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private ProjectDuplicatedSchema getProjectDuplicatedSchema(
      String requestId, ProjectRequest projectRequest) {
    return ProjectDuplicatedSchema.newBuilder()
        .setEvent(
            ProjectDuplicated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Project.Duplicated")
                .setPayload(getNewProject(projectRequest))
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private ProjectCreatedSchema getProjectCreatedSchema(String requestId, Project project) {
    return ProjectCreatedSchema.newBuilder()
        .setEvent(
            ProjectCreated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Project.Created")
                .setPayload(getProject(project))
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private VersionCreatedSchema getVersionCreatedSchema(
      String requestId, String projectId, ProjectVersion projectVersion) {
    return VersionCreatedSchema.newBuilder()
        .setEvent(
            VersionCreated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Project.Version.Created")
                .setProjectId(projectId)
                .setPayload(getVersion(projectVersion))
                .setPublisher("Service.Project")
                .build())
        .build();
  }

  private NewProject getNewProject(ProjectRequest projectRequest) {
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
