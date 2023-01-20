package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
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
      IssueRequestResponse.Priority.class,
      IssueRequestResponse.Severity.class
    })
public interface IssueMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
    @Mapping(
        target = "priority",
        source = "createIssueRequest.priority",
        qualifiedByName = "priorityToValue"),
    @Mapping(
        target = "severity",
        source = "createIssueRequest.severity",
        qualifiedByName = "severityToValue"),
    @Mapping(target = "reporter", source = "reporter"),
    @Mapping(target = "watchers", expression = "java(Set.of())"),
    @Mapping(target = "lastUpdateBy", source = "reporter"),
    @Mapping(target = "assignee", ignore = true),
    @Mapping(target = "endedAt", ignore = true)
  })
  IssueEntity issueRequestToIssueEntity(
      String reporter, IssueRequestResponse.CreateIssueRequest createIssueRequest);

  @Mappings({
    @Mapping(
        target = "priority",
        expression =
            "java(Optional.ofNullable(updateIssueRequest.priority()).map(IssueRequestResponse.Priority::getValue).orElse(issueEntity.getPriority()))"),
    @Mapping(
        target = "severity",
        expression =
            "java(Optional.ofNullable(updateIssueRequest.severity()).map(IssueRequestResponse.Severity::getValue).orElse(issueEntity.getSeverity()))"),
    @Mapping(
        target = "businessUnit",
        expression =
            "java(Optional.ofNullable(updateIssueRequest.businessUnit()).orElse(issueEntity.getBusinessUnit()))"),
    @Mapping(
        target = "header",
        expression =
            "java(Optional.ofNullable(updateIssueRequest.header()).orElse(issueEntity.getHeader()))"),
    @Mapping(
        target = "description",
        expression =
            "java(Optional.ofNullable(updateIssueRequest.description()).orElse(issueEntity.getDescription()))"),
    @Mapping(
        target = "tags",
        expression =
            "java(Optional.ofNullable(updateIssueRequest.tags()).orElse(issueEntity.getTags()))"),
    @Mapping(target = "lastUpdateBy", source = "updateBy"),
  })
  IssueEntity issueRequestToIssueEntity(
      String updateBy,
      IssueEntity issueEntity,
      IssueRequestResponse.UpdateIssueRequest updateIssueRequest);

  @Mappings({
    @Mapping(target = "priority", source = "priority", qualifiedByName = "valueToPriority"),
    @Mapping(target = "severity", source = "severity", qualifiedByName = "valueToSeverity"),
  })
  IssueRequestResponse.IssueResponse issueEntityToIssue(IssueEntity issueEntity);

  @Named("priorityToValue")
  default Integer priorityToValue(IssueRequestResponse.Priority priority) {
    return priority.getValue();
  }

  @Named("severityToValue")
  default Integer severityToValue(IssueRequestResponse.Severity severity) {
    return severity.getValue();
  }

  @Named("valueToPriority")
  default IssueRequestResponse.Priority valueToPriority(Integer priorityValue) {
    return IssueRequestResponse.Priority.fromValue(priorityValue);
  }

  @Named("valueToSeverity")
  default IssueRequestResponse.Severity valueToSeverity(Integer severityValue) {
    return IssueRequestResponse.Severity.fromValue(severityValue);
  }
}
