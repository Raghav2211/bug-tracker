package com.github.devraghav.issue.mapper;

import com.github.devraghav.issue.dto.*;
import com.github.devraghav.issue.entity.IssueEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    imports = {UUID.class, LocalDateTime.class, Optional.class, Priority.class, Severity.class})
public interface IssueMapper {

  IssueMapper INSTANCE = Mappers.getMapper(IssueMapper.class);

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
    @Mapping(target = "priority", source = "priority", qualifiedByName = "priorityToValue"),
    @Mapping(target = "severity", source = "severity", qualifiedByName = "severityToValue"),
    @Mapping(target = "watchers", expression = "java(Set.of())"),
    @Mapping(target = "assignee", ignore = true),
    @Mapping(target = "endedAt", ignore = true)
  })
  IssueEntity issueRequestToIssueEntity(IssueRequest issueRequest);

  @Mappings({
    @Mapping(
        target = "priority",
        expression =
            "java(Optional.ofNullable(issueUpdateRequest.priority()).map(Priority::getValue).orElse(issueEntity.getPriority()))"),
    @Mapping(
        target = "severity",
        expression =
            "java(Optional.ofNullable(issueUpdateRequest.severity()).map(Severity::getValue).orElse(issueEntity.getSeverity()))"),
    @Mapping(
        target = "businessUnit",
        expression =
            "java(Optional.ofNullable(issueUpdateRequest.businessUnit()).orElse(issueEntity.getBusinessUnit()))"),
    @Mapping(
        target = "header",
        expression =
            "java(Optional.ofNullable(issueUpdateRequest.header()).orElse(issueEntity.getHeader()))"),
    @Mapping(
        target = "description",
        expression =
            "java(Optional.ofNullable(issueUpdateRequest.description()).orElse(issueEntity.getDescription()))"),
    @Mapping(
        target = "tags",
        expression =
            "java(Optional.ofNullable(issueUpdateRequest.tags()).orElse(issueEntity.getTags()))"),
    @Mapping(target = "assignee", ignore = true)
  })
  IssueEntity issueRequestToIssueEntity(
      IssueEntity issueEntity, IssueUpdateRequest issueUpdateRequest);

  @Mappings({
    @Mapping(target = "priority", source = "priority", qualifiedByName = "valueToPriority"),
    @Mapping(target = "severity", source = "severity", qualifiedByName = "valueToSeverity"),
    @Mapping(target = "assignee", ignore = true),
    @Mapping(target = "reporter", ignore = true),
    @Mapping(target = "watchers", ignore = true),
    @Mapping(target = "projects", ignore = true),
    @Mapping(target = "comments", ignore = true),
  })
  Issue.IssueBuilder issueEntityToIssue(IssueEntity issueEntity);

  @Named("priorityToValue")
  default Integer priorityToValue(Priority priority) {
    return priority.getValue();
  }

  @Named("severityToValue")
  default Integer severityToValue(Severity severity) {
    return severity.getValue();
  }

  @Named("valueToPriority")
  default Priority valueToPriority(Integer priorityValue) {
    return Priority.fromValue(priorityValue);
  }

  @Named("valueToSeverity")
  default Severity valueToSeverity(Integer severityValue) {
    return Severity.fromValue(severityValue);
  }
}
