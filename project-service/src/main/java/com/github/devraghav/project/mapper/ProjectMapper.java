package com.github.devraghav.project.mapper;

import com.github.devraghav.project.dto.*;
import com.github.devraghav.project.entity.ProjectEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(
    uses = {ProjectVersionMapper.class},
    componentModel = "spring",
    imports = {List.class, LocalDateTime.class, UUID.class})
public interface ProjectMapper {
  ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "enabled", constant = "true"),
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToValue"),
    @Mapping(target = "versions", expression = "java(List.of())"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
  })
  ProjectEntity requestToEntity(ProjectRequest projectRequest);

  @Mappings({
    @Mapping(target = "status", source = "status", qualifiedByName = "valueToStatus"),
    @Mapping(target = "author", ignore = true)
  })
  Project.ProjectBuilder entityToResponse(ProjectEntity projectEntity);

  @Named("statusToValue")
  default Integer statusToValue(ProjectStatus projectStatus) {
    return switch (projectStatus) {
      case DEPLOYED, IN_PROGRESS, POC, UNKNOWN -> projectStatus.getValue();
    };
  }

  @Named("valueToStatus")
  default ProjectStatus valueToStatus(Integer projectStatusValue) {
    return switch (projectStatusValue) {
      case -1, 0, 1, 2 -> ProjectStatus.fromValue(projectStatusValue);
      default -> throw ProjectException.unrecognizedStatus();
    };
  }
}
