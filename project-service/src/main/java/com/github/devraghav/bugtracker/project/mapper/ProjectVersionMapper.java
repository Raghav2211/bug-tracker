package com.github.devraghav.bugtracker.project.mapper;

import com.github.devraghav.bugtracker.project.dto.ProjectVersion;
import com.github.devraghav.bugtracker.project.dto.ProjectVersionRequest;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(
    componentModel = "spring",
    imports = {UUID.class})
public interface ProjectVersionMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
  })
  ProjectVersionEntity requestToEntity(ProjectVersionRequest projectRequest);

  ProjectVersion entityToResponse(ProjectVersionEntity projectEntity);
}
