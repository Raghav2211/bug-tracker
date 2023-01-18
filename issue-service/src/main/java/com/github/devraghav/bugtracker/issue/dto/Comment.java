package com.github.devraghav.bugtracker.issue.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Comment implements Comparable<Comment> {
  private String id;
  private String issueId;
  private User user;
  private String content;
  private LocalDateTime createdAt;
  private LocalDateTime lastUpdatedAt;

  public static class CommentBuilder {
    public CommentBuilder() {}
  }

  @Override
  public int compareTo(Comment o) {
    return o.createdAt.compareTo(this.getCreatedAt());
  }
}
