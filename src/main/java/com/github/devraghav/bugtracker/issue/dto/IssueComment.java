package com.github.devraghav.bugtracker.issue.dto;

import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import com.github.devraghav.bugtracker.user.dto.User;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueComment implements Comparable<IssueComment> {
  private String id;
  private User user;
  private String content;
  private LocalDateTime createdAt;

  private IssueComment(IssueCommentEntity issueCommentEntity) {
    this.id = issueCommentEntity.getId();
    this.content = issueCommentEntity.getContent();
    this.createdAt = issueCommentEntity.getCreatedAt();
  }

  public IssueComment(IssueCommentEntity issueCommentEntity, User user) {
    this(issueCommentEntity);
    this.user = user;
  }

  @Override
  public int compareTo(IssueComment o) {
    return o.createdAt.compareTo(this.getCreatedAt());
  }
}
