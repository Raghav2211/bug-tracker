package com.github.devraghav.bugtracker.project.mapper;

import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.exception.ProjectException;
import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import com.github.devraghav.bugtracker.project.response.ProjectResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(
    uses = {ProjectVersionMapper.class},
    componentModel = "spring",
    imports = {List.class, LocalDateTime.class, UUID.class})
public interface ProjectMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "enabled", constant = "true"),
    @Mapping(target = "status", source = "createProject.status", qualifiedByName = "statusToValue"),
    @Mapping(target = "versions", expression = "java(List.of())"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
    @Mapping(target = "author", source = "author")
  })
  ProjectEntity requestToEntity(String author, ProjectRequest.CreateProject createProject);

  @Mappings({@Mapping(target = "status", source = "status", qualifiedByName = "valueToStatus")})
  ProjectResponse.Project entityToResponse(ProjectEntity projectEntity);

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
