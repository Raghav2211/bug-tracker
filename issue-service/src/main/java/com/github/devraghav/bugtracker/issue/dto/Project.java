package com.github.devraghav.bugtracker.issue.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record Project(
    String id,
    String name,
    String description,
    Boolean enabled,
    ProjectStatus status,
    String author,
    LocalDateTime createdAt,
    Set<ProjectVersion> versions) {}
