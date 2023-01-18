package com.github.devraghav.bugtracker.issue.entity;

import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("comments")
@NoArgsConstructor
@Builder(toBuilder = true)
public class CommentEntity {
  @Id private String id;
  private String issueId;
  private String userId;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime lastUpdatedAt;
}
