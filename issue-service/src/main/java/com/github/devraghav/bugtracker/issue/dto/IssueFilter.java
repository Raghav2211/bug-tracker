package com.github.devraghav.bugtracker.issue.dto;

import com.github.devraghav.bugtracker.issue.request.IssueRequest;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;

@Builder
@Getter
public class IssueFilter {
  private String projectId;
  private String reportedBy;
  private IssueRequest.Page pageRequest;

  public PageRequest getPageRequest() {
    return PageRequest.of(pageRequest.page(), pageRequest.size()).withSort(pageRequest.sort());
  }

  public Optional<String> getProjectId() {
    return Optional.ofNullable(projectId);
  }

  public Optional<String> getReportedBy() {
    return Optional.ofNullable(reportedBy);
  }
}
