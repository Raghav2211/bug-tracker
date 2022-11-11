package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.IssueComment;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import com.github.devraghav.bugtracker.issue.repository.IssueCommentRepository;
import com.github.devraghav.bugtracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IssueCommentFetchService {
  private final IssueCommentRepository issueCommentRepository;
  private final UserService userService;

  public Flux<IssueComment> getComments(String issueId) {
    return issueCommentRepository.findAllByIssueId(issueId).flatMap(this::getComment);
  }

  public Mono<IssueComment> getComment(IssueCommentEntity issueCommentEntity) {
    return userService
        .findById(issueCommentEntity.getUserId())
        .map(commentUser -> new IssueComment(issueCommentEntity, commentUser));
  }
}
