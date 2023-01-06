package com.github.devraghav.bugtracker.issue.dto;

public record UpdateCommentRequest(String userId, String issueId, String commentId, String content)
    implements CommentRequest {}
