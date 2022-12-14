package com.github.devraghav.issue.entity;

import com.github.devraghav.issue.dto.IssueCommentRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("comments")
public class IssueCommentEntity {
  @Id private String id;
  private String issueId;
  private String userId;
  private String content;
  private LocalDateTime createdAt;

  public IssueCommentEntity(IssueCommentRequest request, String issueId) {
    this.id = UUID.randomUUID().toString();
    this.issueId = issueId;
    this.userId = request.getUserId();
    this.content = request.getContent();
    this.createdAt = LocalDateTime.now();
  }
}
