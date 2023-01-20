package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.request.CommentRequest;
import com.github.devraghav.bugtracker.issue.response.CommentResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    imports = {LocalDateTime.class, UUID.class})
public interface CommentMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
    @Mapping(target = "lastUpdatedAt", expression = "java(LocalDateTime.now())"),
  })
  CommentEntity requestToEntity(CommentRequest.CreateComment createComment);

  CommentResponse.Comment entityToResponse(CommentEntity commentEntity);
}
