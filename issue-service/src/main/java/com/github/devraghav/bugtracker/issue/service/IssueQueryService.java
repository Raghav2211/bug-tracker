package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.entity.ProjectInfoRef;
import com.github.devraghav.bugtracker.issue.mapper.IssueMapper;
import com.github.devraghav.bugtracker.issue.repository.IssueAttachmentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

@Service
public record IssueQueryService(
    IssueMapper issueMapper,
    UserReactiveClient userReactiveClient,
    ProjectReactiveClient projectReactiveClient,
    IssueRepository issueRepository,
    IssueAttachmentRepository issueAttachmentRepository) {

  public Mono<Long> count() {
    return issueRepository.count();
  }

  public Flux<Issue> findAllByFilter(IssueFilter issueFilter) {
    if (issueFilter.getProjectId().isPresent()) {
      return Mono.just(issueFilter.getProjectId())
          .map(Optional::get)
          .flatMapMany(this::getAllByProjectId);

    } else if (issueFilter.getReportedBy().isPresent()) {
      return Mono.just(issueFilter.getReportedBy())
          .map(Optional::get)
          .flatMapMany(this::getAllByReporter);
    }
    return issueRepository.findAllBy(issueFilter.getPageRequest()).flatMap(this::generateIssue);
  }

  public Mono<Issue> get(String issueId) {
    return findById(issueId).flatMap(this::generateIssue);
  }

  public Mono<Boolean> exists(String issueId) {
    return issueRepository
        .existsById(issueId)
        .filter(Boolean::booleanValue)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }

  public Mono<Issue> generateIssue(IssueEntity issueEntity) {
    var watchersMono = getWatchers(issueEntity.getWatchers()).collect(Collectors.toSet());
    var projectsMono = getProjects(issueEntity.getProjects()).collectList();
    var reporterMono = fetchUser(issueEntity.getReporter());

    var assigneeMono =
        issueEntity
            .getAssignee()
            .map(this::getAssignee)
            .or(() -> Optional.of(Mono.just(Optional.empty())))
            .get();
    return Mono.zip(watchersMono, projectsMono, assigneeMono, reporterMono)
        .map(tuple4 -> generateIssue(issueEntity, tuple4));
  }

  public Mono<User> fetchUser(String userId) {
    return userReactiveClient
        .fetchUser(userId)
        .onErrorResume(
            UserClientException.class,
            exception -> Mono.error(IssueException.userServiceException(exception)));
  }

  private Issue generateIssue(
      IssueEntity issueEntity, Tuple4<Set<User>, List<Project>, Optional<User>, User> tuple5) {
    var issueBuilder =
        issueMapper
            .issueEntityToIssue(issueEntity)
            .watchers(tuple5.getT1())
            .projects(tuple5.getT2())
            .reporter(tuple5.getT4())
            .endedAt(issueEntity.getEndedAt());
    tuple5.getT3().ifPresent(issueBuilder::assignee);
    return issueBuilder.build();
  }

  private Flux<User> getWatchers(Set<String> watchers) {
    return Flux.fromIterable(watchers).flatMap(this::fetchUser);
  }

  private Flux<Project> getProjects(Set<ProjectInfoRef> projectInfoRefs) {
    return Flux.fromIterable(projectInfoRefs).flatMap(this::getProject);
  }

  private Flux<Issue> getAllByReporter(String reporter) {
    return fetchUser(reporter)
        .flatMapMany(
            unused -> issueRepository.findAllByReporter(reporter).flatMap(this::generateIssue));
  }

  private Mono<Optional<User>> getAssignee(String assignee) {
    return Mono.just(assignee).flatMap(this::fetchUser).map(Optional::of);
  }

  private Mono<Project> getProject(ProjectInfoRef projectInfoRef) {
    return projectReactiveClient
        .fetchProject(projectInfoRef.getProjectId())
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)))
        .doOnNext(project -> project.removeProjectVersionIfNotEqual(projectInfoRef.getVersionId()));
  }

  public Mono<IssueEntity> findById(String issueId) {
    return issueRepository
        .findById(issueId)
        .switchIfEmpty(Mono.error(() -> IssueException.invalidIssue(issueId)));
  }

  private Flux<Issue> getAllByProjectId(String projectId) {
    return validateProjectId(projectId)
        .flatMapMany(
            unused -> issueRepository.findAllByProjectId(projectId).flatMap(this::generateIssue));
  }

  private Mono<Boolean> validateProjectId(String projectId) {
    return projectReactiveClient
        .isProjectExists(projectId)
        .onErrorResume(
            ProjectClientException.class,
            exception -> Mono.error(IssueException.projectServiceException(exception)));
  }
}
