package com.github.devraghav.bugtracker.issue.kafka.producer;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.dto.Issue;
import com.github.devraghav.data_model.domain.issue.*;
import com.github.devraghav.data_model.domain.issue.comment.Comment;
import com.github.devraghav.data_model.domain.project.version.Version;
import com.github.devraghav.data_model.domain.user.User;
import com.github.devraghav.data_model.event.issue.*;
import com.github.devraghav.data_model.event.issue.comment.CommentAdded;
import com.github.devraghav.data_model.event.issue.comment.CommentUpdated;
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

  public Mono<CreateIssueRequest> sendIssueDuplicatedEvent(
      String requestId, CreateIssueRequest createIssueRequest) {
    var event = getIssueDuplicatedSchema(requestId, createIssueRequest);
    log.atDebug().log("IssueDuplicatedEvent {}", event);
    return send(event).thenReturn(createIssueRequest);
  }

  public Mono<Issue> sendIssueCreatedEvent(String requestId, Issue issue) {
    log.atDebug().log("create issue created event using request {} issue {}", requestId, issue);
    var event = getIssueCreatedSchema(requestId, issue);
    log.atDebug().log("IssueCreatedEvent {}", event);
    return send(event).thenReturn(issue);
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

  public Mono<IssueComment> sendCommentAddedEvent(
      String requestId, String issueId, IssueComment issueComment) {
    var event = getCommentAddedSchema(requestId, issueId, issueComment);
    log.atDebug().log("CommentAddedEvent {}", event);
    return send(event).thenReturn(issueComment);
  }

  public Mono<IssueComment> sendCommentUpdatedEvent(
      String requestId, String issueId, IssueComment issueComment) {
    var event = getCommentUpdatedSchema(requestId, issueId, issueComment);
    log.atDebug().log("CommentUpdatedEvent {}", event);
    return send(event).thenReturn(issueComment);
  }

  public Mono<String> sendIssueResolvedEvent(
      String requestId, String issueId, LocalDateTime resolveLocalDateTime) {
    var event = getIssueResolvedSchema(requestId, issueId, resolveLocalDateTime);
    log.atDebug().log("IssueResolvedEvent {}", event);
    return send(event).thenReturn(issueId);
  }

  private IssueDuplicatedSchema getIssueDuplicatedSchema(
      String requestId, CreateIssueRequest createIssueRequest) {
    return IssueDuplicatedSchema.newBuilder()
        .setEvent(
            IssueDuplicated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Issue.Duplicated")
                .setPayload(getNewIssue(createIssueRequest))
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

  private CommentAddedSchema getCommentAddedSchema(
      String requestId, String issueId, IssueComment issueComment) {
    return CommentAddedSchema.newBuilder()
        .setEvent(
            CommentAdded.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Comment.Added")
                .setPayload(getComment(issueComment))
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private CommentUpdatedSchema getCommentUpdatedSchema(
      String requestId, String issueId, IssueComment issueComment) {
    return CommentUpdatedSchema.newBuilder()
        .setEvent(
            CommentUpdated.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Comment.Updated")
                .setPayload(getComment(issueComment))
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private IssueResolvedSchema getIssueResolvedSchema(
      String requestId, String issueId, LocalDateTime localDateTime) {
    return IssueResolvedSchema.newBuilder()
        .setEvent(
            IssueResolved.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRequestId(requestId)
                .setCreateAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .setName("Issue.Issue.Resolved")
                .setPayload(
                    Resolve.newBuilder()
                        .setEndedAt(localDateTime.toEpochSecond(ZoneOffset.UTC))
                        .build())
                .setPublisher("Service.Issue")
                .build())
        .build();
  }

  private NewIssue getNewIssue(CreateIssueRequest createIssueRequest) {
    var tags =
        createIssueRequest.tags().entrySet().stream()
            .collect(Collectors.toMap(Objects::toString, Object::toString));
    return NewIssue.newBuilder()
        .setHeader(createIssueRequest.header())
        .setDescription(createIssueRequest.description())
        .setBusinessUnit(createIssueRequest.businessUnit())
        .setPriority(createIssueRequest.priority().name())
        .setSeverity(createIssueRequest.severity().name())
        .setProjectAttachments(getProjectAttachments(createIssueRequest.projects()))
        .setReporterId(createIssueRequest.reporter())
        .setTags(createIssueRequest.tags())
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
