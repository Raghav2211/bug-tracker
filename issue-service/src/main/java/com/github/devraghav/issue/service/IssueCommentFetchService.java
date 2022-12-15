package com.github.devraghav.issue.service;

import com.github.devraghav.issue.dto.IssueComment;
import com.github.devraghav.issue.entity.IssueCommentEntity;
import com.github.devraghav.issue.mapper.IssueCommentMapper;
import com.github.devraghav.issue.repository.IssueCommentRepository;
import com.github.devraghav.user.UserReactiveClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record IssueCommentFetchService(
    IssueCommentMapper issueCommentMapper,
    UserReactiveClient userReactiveClient,
    IssueCommentRepository issueCommentRepository) {

  public Flux<IssueComment> getComments(String issueId) {
    return issueCommentRepository.findAllByIssueId(issueId).flatMap(this::getComment);
  }

  public Mono<IssueComment> getComment(IssueCommentEntity issueCommentEntity) {
    return userReactiveClient
        .fetchUser(issueCommentEntity.getUserId())
        .map(
            commentUser ->
                issueCommentMapper.entityToResponse(issueCommentEntity).user(commentUser).build());
  }
}
