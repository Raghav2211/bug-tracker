package com.github.devraghav.bugtracker.issue.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IssueComment implements Comparable<IssueComment> {
  private String id;
  private User user;
  private String content;
  private LocalDateTime createdAt;

  public static class IssueCommentBuilder {
    public IssueCommentBuilder() {}
  }

  @Override
  public int compareTo(IssueComment o) {
    return o.createdAt.compareTo(this.getCreatedAt());
  }
}
