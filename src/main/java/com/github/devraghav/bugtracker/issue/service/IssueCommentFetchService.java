package com.github.devraghav.bugtracker.issue.service;

import com.github.devraghav.bugtracker.issue.dto.IssueComment;
import com.github.devraghav.bugtracker.issue.dto.IssueException;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import com.github.devraghav.bugtracker.issue.repository.IssueCommentRepository;
import com.github.devraghav.bugtracker.user.dto.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IssueCommentFetchService {
  private final String userFindByIdURL;
  private final WebClient webClient;
  private final IssueCommentRepository issueCommentRepository;

  public IssueCommentFetchService(
      @Value("${app.external.user-service.url}") String userServiceURL,
      WebClient webClient,
      IssueCommentRepository issueCommentRepository) {

    this.webClient = webClient;
    this.userFindByIdURL = userServiceURL + "/api/rest/v1/user/{id}";
    this.issueCommentRepository = issueCommentRepository;
  }

  public Flux<IssueComment> getComments(String issueId) {
    return issueCommentRepository.findAllByIssueId(issueId).flatMap(this::getComment);
  }

  public Mono<IssueComment> getComment(IssueCommentEntity issueCommentEntity) {
    return getUser(issueCommentEntity.getUserId())
        .map(commentUser -> new IssueComment(issueCommentEntity, commentUser));
  }

  public Mono<User> getUser(String userId) {
    return webClient
        .get()
        .uri(userFindByIdURL, userId)
        .retrieve()
        .onStatus(
            httpStatusCode -> httpStatusCode.value() == HttpStatus.NOT_FOUND.value(),
            clientResponse -> Mono.error(IssueException.invalidUser(userId)))
        .bodyToMono(User.class);
  }
}
