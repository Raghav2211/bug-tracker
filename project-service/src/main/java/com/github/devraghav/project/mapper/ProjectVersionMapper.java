package com.github.devraghav.project.mapper;

import com.github.devraghav.project.dto.*;
import com.github.devraghav.project.entity.ProjectVersionEntity;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    imports = {UUID.class})
public interface ProjectVersionMapper {
  ProjectVersionMapper INSTANCE = Mappers.getMapper(ProjectVersionMapper.class);

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
  })
  ProjectVersionEntity requestToEntity(ProjectVersionRequest projectRequest);

  ProjectVersion entityToResponse(ProjectVersionEntity projectEntity);
}
