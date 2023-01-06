package com.github.devraghav.bugtracker.issue.mapper;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.dto.CreateCommentRequest;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
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
  })
  IssueCommentEntity requestToEntity(CreateCommentRequest createCommentRequest);

  @Mappings({@Mapping(target = "user", ignore = true)})
  Comment.CommentBuilder entityToResponse(IssueCommentEntity issueCommentEntity);
}
