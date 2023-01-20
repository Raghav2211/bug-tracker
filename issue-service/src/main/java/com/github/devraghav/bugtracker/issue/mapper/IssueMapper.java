package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import com.github.devraghav.bugtracker.issue.response.IssueResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    imports = {
      UUID.class,
      LocalDateTime.class,
      Optional.class,
      IssueResponse.Priority.class,
      IssueResponse.Severity.class
    })
public interface IssueMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
    @Mapping(
        target = "priority",
        source = "createIssue.priority",
        qualifiedByName = "priorityToValue"),
    @Mapping(
        target = "severity",
        source = "createIssue.severity",
        qualifiedByName = "severityToValue"),
    @Mapping(target = "reporter", source = "reporter"),
    @Mapping(target = "watchers", expression = "java(Set.of())"),
    @Mapping(target = "lastUpdateBy", source = "reporter"),
    @Mapping(target = "assignee", ignore = true),
    @Mapping(target = "endedAt", ignore = true)
  })
  IssueEntity issueRequestToIssueEntity(String reporter, IssueRequest.CreateIssue createIssue);

  @Mappings({
    @Mapping(
        target = "priority",
        expression =
            "java(Optional.ofNullable(updateIssue.priority()).map(IssueResponse.Priority::getValue).orElse(issueEntity.getPriority()))"),
    @Mapping(
        target = "severity",
        expression =
            "java(Optional.ofNullable(updateIssue.severity()).map(IssueResponse.Severity::getValue).orElse(issueEntity.getSeverity()))"),
    @Mapping(
        target = "businessUnit",
        expression =
            "java(Optional.ofNullable(updateIssue.businessUnit()).orElse(issueEntity.getBusinessUnit()))"),
    @Mapping(
        target = "header",
        expression =
            "java(Optional.ofNullable(updateIssue.header()).orElse(issueEntity.getHeader()))"),
    @Mapping(
        target = "description",
        expression =
            "java(Optional.ofNullable(updateIssue.description()).orElse(issueEntity.getDescription()))"),
    @Mapping(
        target = "tags",
        expression = "java(Optional.ofNullable(updateIssue.tags()).orElse(issueEntity.getTags()))"),
    @Mapping(target = "lastUpdateBy", source = "updateBy"),
  })
  IssueEntity issueRequestToIssueEntity(
      String updateBy, IssueEntity issueEntity, IssueRequest.UpdateIssue updateIssue);

  @Mappings({
    @Mapping(target = "priority", source = "priority", qualifiedByName = "valueToPriority"),
    @Mapping(target = "severity", source = "severity", qualifiedByName = "valueToSeverity"),
  })
  IssueResponse.Issue issueEntityToIssue(IssueEntity issueEntity);

  @Named("priorityToValue")
  default Integer priorityToValue(IssueResponse.Priority priority) {
    return priority.getValue();
  }

  @Named("severityToValue")
  default Integer severityToValue(IssueResponse.Severity severity) {
    return severity.getValue();
  }

  @Named("valueToPriority")
  default IssueResponse.Priority valueToPriority(Integer priorityValue) {
    return IssueResponse.Priority.fromValue(priorityValue);
  }

  @Named("valueToSeverity")
  default IssueResponse.Severity valueToSeverity(Integer severityValue) {
    return IssueResponse.Severity.fromValue(severityValue);
  }
}
