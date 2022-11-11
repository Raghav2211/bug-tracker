package com.github.devraghav.bugtracker.issue.dto;

import java.util.Optional;
import lombok.Data;

@Data
public class IssueFilter {
  private Optional<String> projectId;
  private Optional<String> reportedBy;
}
