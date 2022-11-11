package com.github.devraghav.bugtracker.issue.entity;

import com.github.devraghav.bugtracker.issue.dto.IssueCommentRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class IssueCommentEntity {
  private String id;
  private String userId;
  private String content;
  private LocalDateTime createdAt;

  public IssueCommentEntity(IssueCommentRequest request) {
    this.id = UUID.randomUUID().toString();
    this.userId = request.getUserId();
    this.content = request.getContent();
    this.createdAt = LocalDateTime.now();
  }
}
