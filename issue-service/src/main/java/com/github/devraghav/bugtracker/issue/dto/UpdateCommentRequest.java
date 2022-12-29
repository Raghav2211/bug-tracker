package com.github.devraghav.bugtracker.issue.dto;

public record UpdateCommentRequest(String content) implements CommentRequest {}
