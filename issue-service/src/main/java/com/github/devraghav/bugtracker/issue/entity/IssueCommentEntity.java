package com.github.devraghav.bugtracker.issue.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("comments")
@NoArgsConstructor
public class IssueCommentEntity {
  @Id private String id;
  private String issueId;
  private String userId;
  private String content;
  private LocalDateTime createdAt;
}
