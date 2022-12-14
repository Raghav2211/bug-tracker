package com.github.devraghav.issue.dto;

import java.util.Optional;
import lombok.Data;

@Data
public class IssueFilter {
  private Optional<String> projectId;
  private Optional<String> reportedBy;
}
