package com.github.devraghav.bugtracker.issue.kafka.producer;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.data_model.command.issue.CreateIssue;
import com.github.devraghav.data_model.domain.issue.*;
import com.github.devraghav.data_model.domain.issue.comment.Comment;
import com.github.devraghav.data_model.domain.project.version.Version;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.*;
import com.github.devraghav.data_model.schema.issue.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
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

  public Mono<IssueRequest> sendIssueCreateCommand(String requestId, IssueRequest issueRequest) {
    var command = getCreateIssueSchema(requestId, issueRequest);
    log.atDebug().log("IssueCreateCommand {}", command);
    return send(command).thenReturn(issueRequest);
  }

  public Mono<IssueRequest> sendIssueDuplicatedEvent(String requestId, IssueRequest issueRequest) {
    var event = getIssueDuplicatedSchema(requestId, issueRequest);
    log.atDebug().log("IssueDuplicatedEvent {}", event);
    return send(event).thenReturn(issueRequest);
  }

  public Mono<Issue> sendIssueCreatedEvent(String requestId, Issue issue) {
    log.atDebug().log("create issue created event using request {} issue {}", requestId, issue);
    var event = getIssueCreatedSchema(requestId, issue);
    log.atDebug().log("IssueCreatedEvent {}", event);
    return send(event).thenReturn(issue);
  }

  public Mono<IssueUpdateRequest> sendIssueUpdateCommand(
      String requestId, IssueUpdateRequest IssueUpdateRequest) {
    var command = getUpdateIssueSchema(requestId, IssueUpdateRequest);
    log.atDebug().log("IssueUpdateCommand {}", command);
    return send(command).thenReturn(IssueUpdateRequest);
  }

  public Mono<Issue> sendIssueUpdatedEvent(String requestId, Issue issue) {
    var event = getIssueUpdatedSchema(requestId, issue);
    log.atDebug().log("IssueUpdatedEvent {}", event);
    return send(event).thenReturn(issue);
  }

  public Mono<String> sendIssueAssignedEvent(
      String requestId, String issueId, com.github.devraghav.bugtracker.issue.dto.User user) {
    var event = getIssueAssignedSchema(requestId, issueId, user);
    log.atDebug().log("IssueAssigned {}", event);
    return send(event).thenReturn(issueId);
  }

  public Mono<String> sendIssueUnassignedEvent(String requestId, String issueId) {
    var event = getIssueUnassignedSchema(requestId, issueId);
    log.atDebug().log("IssueUnassigned {}", event);
    return send(event).thenReturn(issueId);
  }

  public Mono<String> sendIssueWatchedEvent(
      String requestId, String issueId, com.github.devraghav.bugtracker.issue.dto.User user) {
    var event = getIssueWatchedSchema(requestId, issueId, user);
    log.atDebug().log("IssueWatchedEvent {}", event);
    return send(event).thenReturn(issueId);
  }

  public Mono<String> sendIssueUnwatchedEvent(
      String requestId, String issueId, com.github.devraghav.bugtracker.issue.dto.User user) {
    var event = getIssueUnwatchedSchema(requestId, issueId, user);
    log.atDebug().log("IssueUnwatched {}", event);
    return send(event).thenReturn(issueId);
  }

  private CreateIssueSchema getCreateIssueSchema(String requestId, IssueRequest projectRequest) {
    return CreateIssueSchema.newBuilder()
        .setCommand(
            CreateIssue.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Issue.Create")
                .setPayload(getNewIssue(projectRequest))
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private UpdateIssueSchema getUpdateIssueSchema(
      String requestId, IssueUpdateRequest issueUpdateRequest) {
    return UpdateIssueSchema.newBuilder()
        .setCommand(
            com.github.devraghav.data_model.command.issue.UpdateIssue.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Issue.Update")
                .setPayload(getUpdateIssue(issueUpdateRequest))
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueDuplicatedSchema getIssueDuplicatedSchema(
      String requestId, IssueRequest issueRequest) {
    return IssueDuplicatedSchema.newBuilder()
        .setEvent(
            IssueDuplicated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Issue.Duplicated")
                .setPayload(getNewIssue(issueRequest))
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueCreatedSchema getIssueCreatedSchema(String requestId, Issue issue) {
    return IssueCreatedSchema.newBuilder()
        .setEvent(
            IssueCreated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Issue.Created")
                .setPayload(getIssue(issue))
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueAssignedSchema getIssueAssignedSchema(
      String requestId, String issueId, com.github.devraghav.bugtracker.issue.dto.User user) {
    return IssueAssignedSchema.newBuilder()
        .setEvent(
            IssueAssigned.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Assign.Added")
                .setPayload(
                    Assign.newBuilder().setIssueId(issueId).setAssignee(getUser(user)).build())
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueUnassignedSchema getIssueUnassignedSchema(String requestId, String issueId) {
    return IssueUnassignedSchema.newBuilder()
        .setEvent(
            IssueUnassigned.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Unassign.Removed")
                .setPayload(Unassign.newBuilder().setIssueId(issueId).build())
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueWatchedSchema getIssueWatchedSchema(
      String requestId, String issueId, com.github.devraghav.bugtracker.issue.dto.User user) {
    return IssueWatchedSchema.newBuilder()
        .setEvent(
            IssueWatched.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Watcher.Added")
                .setPayload(
                    Watcher.newBuilder().setIssueId(issueId).setWatcher(getUser(user)).build())
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueUnwatchedSchema getIssueUnwatchedSchema(
      String requestId, String issueId, com.github.devraghav.bugtracker.issue.dto.User user) {
    return IssueUnwatchedSchema.newBuilder()
        .setEvent(
            IssueUnwatched.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Watcher.Removed")
                .setPayload(
                    Unwatch.newBuilder()
                        .setIssueId(issueId)
                        .setRemoveWatcher(getUser(user))
                        .build())
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueUpdatedSchema getIssueUpdatedSchema(String requestId, Issue issue) {
    return IssueUpdatedSchema.newBuilder()
        .setEvent(
            IssueUpdated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Issue.Updated")
                .setPayload(getIssue(issue))
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private NewIssue getNewIssue(IssueRequest issueRequest) {
    var tags =
        issueRequest.tags().entrySet().stream()
            .collect(Collectors.toMap(Objects::toString, Object::toString));
    return NewIssue.newBuilder()
        .setHeader(issueRequest.header())
        .setDescription(issueRequest.description())
        .setBusinessUnit(issueRequest.businessUnit())
        .setPriority(issueRequest.priority().name())
        .setSeverity(issueRequest.severity().name())
        .setProjectAttachments(getProjectAttachments(issueRequest.projects()))
        .setReporterId(issueRequest.reporter())
        .setTags(issueRequest.tags())
        .build();
  }

  private UpdateIssue getUpdateIssue(IssueUpdateRequest issueUpdateRequest) {
    var tags =
        issueUpdateRequest.tags().entrySet().stream()
            .collect(Collectors.toMap(Objects::toString, Object::toString));
    return UpdateIssue.newBuilder()
        .setHeader(issueUpdateRequest.header())
        .setDescription(issueUpdateRequest.description())
        .setBusinessUnit(issueUpdateRequest.businessUnit())
        .setPriority(issueUpdateRequest.priority().name())
        .setSeverity(issueUpdateRequest.severity().name())
        // TODO : update project attachment feature
        //            .setProjectAttachments(getProjectAttachments(issueUpdateRequest.projects()))
        .setTags(issueUpdateRequest.tags())
        .build();
  }

  private com.github.devraghav.data_model.domain.issue.Issue getIssue(Issue issue) {
    var issueBuilder =
        com.github.devraghav.data_model.domain.issue.Issue.newBuilder()
            .setId(issue.getId())
            .setHeader(issue.getHeader())
            .setDescription(issue.getDescription())
            .setBusinessUnit(issue.getBusinessUnit())
            .setPriority(issue.getPriority().name())
            .setSeverity(issue.getSeverity().name())
            .setAssignee(getAssignee(issue.getAssignee()))
            .setProjects(getProjects(issue.getProjects()))
            .setWatchers(getWatchers(issue.getWatchers()))
            .setComments(getComments(issue.getComments()))
            .setReporter(getUser(issue.getReporter()))
            .setTags(issue.getTags())
            .setCreatedAt(issue.getCreatedAt().toEpochSecond(ZoneOffset.UTC));
    if (issue.getEndedAt() != null) {
      issueBuilder.setEndedAt(issue.getEndedAt().toEpochSecond(ZoneOffset.UTC));
    }
    return issueBuilder.build();
  }

  private User getAssignee(com.github.devraghav.bugtracker.issue.dto.User assignee) {
    return assignee == null ? null : getUser(assignee);
  }

  private List<User> getWatchers(Set<com.github.devraghav.bugtracker.issue.dto.User> watchers) {
    return watchers == null
        ? null
        : watchers.stream().map(this::getUser).collect(Collectors.toList());
  }

  private User getUser(com.github.devraghav.bugtracker.issue.dto.User author) {
    return User.newBuilder()
        .setId(author.id())
        .setAccessLevel(author.access().name())
        .setEmail(author.email())
        .setEnabled(author.enabled())
        .setFirstName(author.firstName())
        .setLastName(author.lastName())
        .build();
  }

  private List<com.github.devraghav.data_model.domain.project.Project> getProjects(
      List<Project> projects) {
    return projects.stream().map(this::getProject).collect(Collectors.toList());
  }

  private com.github.devraghav.data_model.domain.project.Project getProject(Project project) {
    return com.github.devraghav.data_model.domain.project.Project.newBuilder()
        .setId(project.id())
        .setName(project.name())
        .setAuthor(getUser(project.author()))
        .setDescription(project.description())
        .setEnabled(project.enabled())
        .setCreatedAt(project.createdAt().toEpochSecond(ZoneOffset.UTC))
        .setStatus(project.status().name())
        .setVersions(getVersions(project.versions()))
        .build();
  }

  private List<Version> getVersions(Set<ProjectVersion> versions) {
    return versions.stream().map(this::getVersion).collect(Collectors.toList());
  }

  private Version getVersion(ProjectVersion version) {
    return Version.newBuilder().setId(version.id()).setVersion(version.version()).build();
  }

  private List<ProjectAttachment> getProjectAttachments(Set<ProjectInfo> projectInfos) {
    return projectInfos.stream().map(this::getProjectAttachment).collect(Collectors.toList());
  }

  private ProjectAttachment getProjectAttachment(ProjectInfo projectInfo) {
    return ProjectAttachment.newBuilder()
        .setProjectId(projectInfo.projectId())
        .setProjectVersionId(projectInfo.versionId())
        .build();
  }

  private List<Comment> getComments(List<IssueComment> comments) {
    return comments == null
        ? null
        : comments.stream().map(this::getComment).collect(Collectors.toList());
  }

  private Comment getComment(IssueComment issueComment) {
    return Comment.newBuilder()
        .setId(issueComment.getId())
        .setContent(issueComment.getContent())
        .setCommenter(getUser(issueComment.getUser()))
        .setCreatedAt(issueComment.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
        .build();
  }
}
