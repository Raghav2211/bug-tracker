package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.exception.ProjectClientException;
import com.github.devraghav.bugtracker.issue.excpetion.IssueException;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueAttachmentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public record IssueQueryService(
    IssueMapper issueMapper,
    ProjectReactiveClient projectReactiveClient,
    IssueRepository issueRepository,
    IssueAttachmentRepository issueAttachmentRepository) {

  public Mono<Long> count() {
    return issueRepository.count();
  }

  public Flux<IssueRequestResponse.IssueResponse> findAllByFilter(IssueFilter issueFilter) {
    if (issueFilter.getProjectId().isPresent()) {
      return Mono.just(issueFilter.getProjectId())
          .map(Optional::get)
          .flatMapMany(this::getAllByProjectId);

    } else if (issueFilter.getReportedBy().isPresent()) {
      return Mono.just(issueFilter.getReportedBy())
          .map(Optional::get)
          .flatMapMany(this::getAllByReporter);
    }
    return issueRepository
        .findAllBy(issueFilter.getPageRequest())
        .map(issueMapper::issueEntityToIssue);
  }

  public Mono<IssueRequestResponse.IssueResponse> get(String issueId) {
    return findById(issueId).map(issueMapper::issueEntityToIssue);
  }

  public Mono<Boolean> exists(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }

  public Mono<IssueEntity> findById(String issueId) {
    return issueRepository
        .findById(issueId)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }

  private Flux<IssueRequestResponse.IssueResponse> getAllByReporter(String reporter) {
    return issueRepository.findAllByReporter(reporter).map(issueMapper::issueEntityToIssue);
  }

  private Flux<IssueRequestResponse.IssueResponse> getAllByProjectId(String projectId) {
    return validateProjectId(projectId)
        .flatMapMany(
            unused ->
                issueRepository.findAllByProjectId(projectId).map(issueMapper::issueEntityToIssue));
  }

  private Mono<Boolean> validateProjectId(String projectId) {
    return projectReactiveClient
        .isProjectExists(projectId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }
}
