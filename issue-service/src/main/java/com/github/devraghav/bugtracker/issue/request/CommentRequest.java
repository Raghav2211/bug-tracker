package com.github.devraghav.bugtracker.issue.request;

public final class CommentRequest {

  public static record CreateComment(String userId, String issueId, String content) {}

  public static record UpdateComment(
      String userId, String issueId, String commentId, String content) {}
}
