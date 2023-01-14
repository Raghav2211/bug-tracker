package com.github.devraghav.bugtracker.project.mapper;

import com.github.devraghav.bugtracker.project.dto.ProjectRequest;
import com.github.devraghav.bugtracker.project.dto.Version;
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
  ProjectVersionEntity requestToEntity(ProjectRequest.CreateVersion projectRequest);

  Version entityToResponse(ProjectVersionEntity projectEntity);
}
