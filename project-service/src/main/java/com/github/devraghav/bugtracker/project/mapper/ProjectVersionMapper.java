package com.github.devraghav.bugtracker.project.mapper;

import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.request.ProjectRequest;
import com.github.devraghav.bugtracker.project.response.ProjectResponse;
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
    @Mapping(target = "userId", source = "userId")
  })
  ProjectVersionEntity requestToEntity(String userId, ProjectRequest.CreateVersion projectRequest);

  ProjectResponse.Version entityToResponse(ProjectVersionEntity projectEntity);
}
