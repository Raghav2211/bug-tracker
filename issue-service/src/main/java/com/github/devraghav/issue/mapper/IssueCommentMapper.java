package com.github.devraghav.issue.mapper;

import com.github.devraghav.issue.dto.IssueComment;
import com.github.devraghav.issue.dto.IssueCommentRequest;
import com.github.devraghav.issue.entity.IssueCommentEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    imports = {LocalDateTime.class, UUID.class})
public interface IssueCommentMapper {

  IssueCommentMapper INSTANCE = Mappers.getMapper(IssueCommentMapper.class);

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "issueId", source = "issueId"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
  })
  IssueCommentEntity requestToEntity(String issueId, IssueCommentRequest issueCommentRequest);

  @Mappings({@Mapping(target = "user", ignore = true)})
  IssueComment.IssueCommentBuilder entityToResponse(IssueCommentEntity issueCommentEntity);
}
