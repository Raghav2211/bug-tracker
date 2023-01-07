package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.Comment;
import com.github.devraghav.bugtracker.issue.entity.CommentEntity;
import com.github.devraghav.bugtracker.issue.mapper.CommentMapper;
import com.github.devraghav.bugtracker.issue.repository.CommentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record CommentQueryService(
    CommentMapper commentMapper,
    UserReactiveClient userReactiveClient,
    CommentRepository commentRepository) {

  public Flux<Comment> getComments(String issueId) {
    return commentRepository.findAllByIssueId(issueId).flatMap(this::getComment);
  }

  public Mono<Comment> getComment(CommentEntity commentEntity) {
    return userReactiveClient
        .fetchUser(commentEntity.getUserId())
        .map(
            commentUser -> commentMapper.entityToResponse(commentEntity).user(commentUser).build());
  }
}
