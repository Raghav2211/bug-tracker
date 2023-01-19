package com.github.devraghav.bugtracker.issue.excpetion;

import com.github.devraghav.bugtracker.issue.exception.UserClientException;
import java.util.Map;
import lombok.Getter;

public class CommentException extends RuntimeException {
  @Getter private final Map<String, Object> meta;

  private CommentException(String message, Map<String, Object> meta) {
    super(message);
    this.meta = meta;
  }

  private CommentException(String message) {
    this(message, Map.of());
  }

  public static CommentException invalidComment(String content) {
    return new CommentException(
        "Comment should not be null & less than 256 character length", Map.of("comment", content));
  }

  public static CommentException notFound(String commentId) {
    return new CommentException("Comment not found", Map.of("commentId", commentId));
  }

  public static CommentException userServiceException(UserClientException userClientException) {
    return new CommentException(userClientException.getMessage(), userClientException.getMeta());
  }
}
