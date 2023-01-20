package com.github.devraghav.bugtracker.issue.dto;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;

@Builder
@Getter
public class IssueFilter {
  private Optional<String> projectId;
  private Optional<String> reportedBy;
  private RequestResponse.Page pageRequest;

  public PageRequest getPageRequest() {
    return PageRequest.of(pageRequest.page(), pageRequest.size()).withSort(pageRequest.sort());
  }
}
