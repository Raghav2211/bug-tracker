package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.dto.IssueComment;
import com.github.devraghav.bugtracker.issue.dto.IssueCommentRequest;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    imports = {LocalDateTime.class, UUID.class})
public interface IssueCommentMapper {

  @Mappings({
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())"),
    @Mapping(target = "issueId", source = "issueId"),
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
  })
  IssueCommentEntity requestToEntity(String issueId, IssueCommentRequest issueCommentRequest);

  @Mappings({@Mapping(target = "user", ignore = true)})
  IssueComment.IssueCommentBuilder entityToResponse(IssueCommentEntity issueCommentEntity);
}
