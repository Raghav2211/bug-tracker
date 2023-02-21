package com.github.devraghav.bugtracker.project.mapper;

import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.exception.ProjectException;
import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import com.github.devraghav.bugtracker.project.response.ProjectResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(
    uses = {ProjectVersionMapper.class},
    componentModel = "spring",
    imports = {List.class, LocalDateTime.class, UUID.class, Optional.class})
public interface ProjectMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "key", source = "name", qualifiedByName = "getKey"),
    @Mapping(target = "enabled", constant = "true"),
    @Mapping(target = "status", source = "createProject.status", qualifiedByName = "statusToValue"),
    @Mapping(target = "versions", expression = "java(List.of())"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
    @Mapping(target = "author", source = "author"),
    @Mapping(target = "createdBy", source = "author"),
    @Mapping(target = "lastUpdateBy", source = "author")
  })
  ProjectEntity requestToEntity(String author, ProjectRequest.CreateProject createProject);

  @Mappings({
    @Mapping(target = "lastUpdateBy", source = "updateBy"),
    @Mapping(
        target = "description",
        expression =
            "java(Optional.ofNullable(updateProject.description()).orElseGet(() ->projectEntity.getDescription()))"),
    @Mapping(
        target = "status",
        expression =
            "java(Optional.ofNullable(updateProject.status()).map(ProjectRequest.ProjectStatus::getValue).orElseGet(() ->projectEntity.getStatus()))"),
    @Mapping(
        target = "tags",
        expression =
            "java(Optional.ofNullable(updateProject.tags()).orElseGet(() ->projectEntity.getTags()))")
  })
  ProjectEntity requestToEntity(
      String updateBy, ProjectEntity projectEntity, ProjectRequest.UpdateProject updateProject);

  @Mappings({@Mapping(target = "status", source = "status", qualifiedByName = "valueToStatus")})
  ProjectResponse.Project entityToResponse(ProjectEntity projectEntity);

  @Named("getKey")
  default String getKey(String name) {
    return Arrays.stream(name.split(" "))
        .map(string -> string.substring(0, 1))
        .map(String::toUpperCase)
        .reduce((first, second) -> first + "" + second)
        .filter(initial -> initial.length() > 1)
        .orElse(name);
  }

  @Named("statusToValue")
  default Integer statusToValue(ProjectRequest.ProjectStatus projectStatus) {
    return switch (projectStatus) {
      case DEPLOYED, IN_PROGRESS, POC, UNKNOWN -> projectStatus.getValue();
    };
  }

  @Named("valueToStatus")
  default ProjectRequest.ProjectStatus valueToStatus(Integer projectStatusValue) {
    return switch (projectStatusValue) {
      case -1, 0, 1, 2 -> ProjectRequest.ProjectStatus.fromValue(projectStatusValue);
      default -> throw ProjectException.unrecognizedStatus();
    };
  }
}
