package com.github.devraghav.bugtracker.issue.dto;

public record CreateCommentRequest(String userId, String issueId, String content) {}
