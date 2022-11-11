package com.github.devraghav.bugtracker.issue.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.github.devraghav.bugtracker.issue.dto.*;
import com.github.devraghav.bugtracker.issue.entity.IssueCommentEntity;
import com.github.devraghav.bugtracker.issue.entity.IssueEntity;
import com.github.devraghav.bugtracker.issue.entity.ProjectInfoRef;
import com.github.devraghav.bugtracker.issue.repository.IssueCommentRepository;
import com.github.devraghav.bugtracker.issue.repository.IssueNotFoundException;
import com.github.devraghav.bugtracker.issue.repository.IssueRepository;
import com.github.devraghav.bugtracker.issue.service.IssueCommentFetchService;
import com.github.devraghav.bugtracker.issue.service.IssueCommentPersistService;
import com.github.devraghav.bugtracker.issue.service.IssueService;
import com.github.devraghav.bugtracker.project.dto.Project;
import com.github.devraghav.bugtracker.project.dto.ProjectVersion;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.repository.ProjectRepository;
import com.github.devraghav.bugtracker.project.repository.ProjectVersionRepository;
import com.github.devraghav.bugtracker.project.service.ProjectService;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.repository.UserNotFoundException;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import com.github.devraghav.bugtracker.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ContextConfiguration(
    classes = {
      IssueRouteDefinition.class,
      IssueRouteHandler.class,
      IssueService.class,
      IssueCommentPersistService.class,
      IssueCommentFetchService.class,
      UserService.class,
      ProjectService.class
    })
@WebFluxTest
@AutoConfigureWebClient
public class IssueRouteTests {

  @Autowired private WebTestClient webClient;

  @MockBean private IssueRepository issueRepository;
  @MockBean private IssueCommentRepository issueCommentRepository;
  @MockBean private UserRepository userRepository;
  @MockBean private ProjectRepository projectRepository;
  @MockBean private ProjectVersionRepository projectVersionRepository;

  @AfterEach
  public void afterEach() {
    verifyNoMoreInteractions(
        issueRepository,
        issueCommentRepository,
        userRepository,
        projectRepository,
        projectVersionRepository);
  }

  @Test
  void test_GetAll() {

    var issueId1 = UUID.randomUUID().toString();

    var projectInfo = new ProjectInfo();
    projectInfo.setProjectId(UUID.randomUUID().toString());
    projectInfo.setVersionId("v1.0");

    var userEntity = new UserEntity();
    userEntity.setEmail("test@test.com");
    userEntity.setEnabled(true);
    userEntity.setLastName("lName");
    userEntity.setFirstName("fName");
    userEntity.setAccess(1);

    var projectEntity = new ProjectEntity();
    projectEntity.setCreatedAt(LocalDateTime.now());
    projectEntity.setAuthor(UUID.randomUUID().toString());
    projectEntity.setStatus(1);
    projectEntity.setName("TestProject");
    projectEntity.setDescription("DESC");
    projectEntity.setId(UUID.randomUUID().toString());

    var projectVersionEntity = new ProjectVersionEntity();
    projectVersionEntity.setId(UUID.randomUUID().toString());
    projectVersionEntity.setVersion("v1.0");

    var issue1 = new IssueEntity();
    issue1.setId(issueId1);

    issue1.setPriority(1);
    issue1.setSeverity(1);

    issue1.setProjects(Set.of(new ProjectInfoRef(projectInfo)));
    issue1.setHeader("Summary for test issue");
    issue1.setDescription("Test issue");
    issue1.setReporter(UUID.randomUUID().toString());

    var issueEntityFlux = Flux.just(issue1);

    when(userRepository.findById(anyString())).thenReturn(Mono.just(userEntity));
    when(projectRepository.findById(anyString())).thenReturn(Mono.just(projectEntity));
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.just(projectVersionEntity));
    when(issueRepository.findAll()).thenReturn(issueEntityFlux);
    when(issueCommentRepository.findAllByIssueId(anyString())).thenReturn(Flux.empty());

    webClient
        .get()
        .uri("/api/rest/v1/issue")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(Issue.class)
        .value(
            projects -> {
              assertEquals(1, projects.size());
              assertEquals(issueId1, projects.get(0).getId());
            });
    verify(issueRepository).findAll();
    verify(issueCommentRepository).findAllByIssueId(anyString());
    verify(userRepository, times(2)).findById(anyString());
    verify(projectRepository).findById(anyString());
    verify(projectVersionRepository).findAll(anyString());
  }

  @Test
  void test_GetAll_WithEmptyResult() {

    when(issueRepository.findAll()).thenReturn(Flux.empty());

    webClient
        .get()
        .uri("/api/rest/v1/issue")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$")
        .isEmpty();
    verify(issueRepository).findAll();
  }

  @Test
  void test_GetAllByProjectId_WithValidProjectId() {
    var projectId = UUID.randomUUID().toString();

    var issueId = UUID.randomUUID().toString();

    var userEntity = new UserEntity();
    userEntity.setEmail("test@test.com");
    userEntity.setEnabled(true);
    userEntity.setLastName("lName");
    userEntity.setFirstName("fName");
    userEntity.setAccess(1);

    var projectEntity = new ProjectEntity();
    projectEntity.setCreatedAt(LocalDateTime.now());
    projectEntity.setAuthor(UUID.randomUUID().toString());
    projectEntity.setStatus(1);
    projectEntity.setName("TestProject");
    projectEntity.setDescription("DESC");
    projectEntity.setId(UUID.randomUUID().toString());

    var projectVersionEntity = new ProjectVersionEntity();
    projectVersionEntity.setId(UUID.randomUUID().toString());
    projectVersionEntity.setVersion("v1.0");

    var projectInfo = new ProjectInfo();
    projectInfo.setProjectId(UUID.randomUUID().toString());
    projectInfo.setVersionId("v1.0");

    var issue1 = new IssueEntity();
    issue1.setId(issueId);

    issue1.setPriority(1);
    issue1.setSeverity(1);

    issue1.setProjects(Set.of(new ProjectInfoRef(projectInfo)));
    issue1.setHeader("Summary for test issue");
    issue1.setDescription("Test issue");
    issue1.setReporter(UUID.randomUUID().toString());

    var issueEntityFlux = Flux.just(issue1);

    when(userRepository.findById(anyString())).thenReturn(Mono.just(userEntity));
    when(projectRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(projectRepository.findById(anyString())).thenReturn(Mono.just(projectEntity));
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.just(projectVersionEntity));
    when(issueRepository.findAllByProjectId(anyString())).thenReturn(issueEntityFlux);
    when(issueCommentRepository.findAllByIssueId(anyString())).thenReturn(Flux.empty());
    webClient
        .get()
        .uri("/api/rest/v1/issue?projectId=" + projectId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(Issue.class)
        .value(
            issues -> {
              assertEquals(1, issues.size());
              assertEquals(issueId, issues.get(0).getId());
            });

    verify(issueRepository).findAllByProjectId(anyString());
    verify(issueCommentRepository).findAllByIssueId(anyString());
    verify(userRepository, times(2)).findById(anyString());
    verify(projectRepository).exists(anyString());
    verify(projectRepository).findById(anyString());
    verify(projectVersionRepository).findAll(anyString());
  }

  @Test
  void test_GetAllByProjectId_WithInValidProjectId() {

    var projectId = UUID.randomUUID().toString();
    var invalidProjectIdException = IssueException.invalidProject(projectId);
    when(projectRepository.exists(anyString())).thenReturn(Mono.just(false));
    webClient
        .get()
        .uri("/api/rest/v1/issue?projectId=" + projectId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo("/api/rest/v1/issue")
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidProjectIdException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(projectRepository).exists(anyString());
  }

  @Test
  void test_GetAllByProjectId_WithValidReporter() {
    var reporter = UUID.randomUUID().toString();

    var issueId = UUID.randomUUID().toString();

    var userEntity = new UserEntity();
    userEntity.setEmail("test@test.com");
    userEntity.setEnabled(true);
    userEntity.setLastName("lName");
    userEntity.setFirstName("fName");
    userEntity.setAccess(1);

    var projectEntity = new ProjectEntity();
    projectEntity.setCreatedAt(LocalDateTime.now());
    projectEntity.setAuthor(UUID.randomUUID().toString());
    projectEntity.setStatus(1);
    projectEntity.setName("TestProject");
    projectEntity.setDescription("DESC");
    projectEntity.setId(UUID.randomUUID().toString());

    var projectVersionEntity = new ProjectVersionEntity();
    projectVersionEntity.setId(UUID.randomUUID().toString());
    projectVersionEntity.setVersion("v1.0");

    var projectInfo = new ProjectInfo();
    projectInfo.setProjectId(UUID.randomUUID().toString());
    projectInfo.setVersionId("v1.0");

    var issue1 = new IssueEntity();
    issue1.setId(issueId);

    issue1.setPriority(1);
    issue1.setSeverity(1);

    issue1.setProjects(Set.of(new ProjectInfoRef(projectInfo)));
    issue1.setHeader("Summary for test issue");
    issue1.setDescription("Test issue");
    issue1.setReporter(reporter);

    var issueEntityFlux = Flux.just(issue1);

    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.findById(anyString())).thenReturn(Mono.just(userEntity));
    when(projectRepository.findById(anyString())).thenReturn(Mono.just(projectEntity));
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.just(projectVersionEntity));

    when(issueRepository.findAllByReporter(anyString())).thenReturn(issueEntityFlux);
    when(issueCommentRepository.findAllByIssueId(anyString())).thenReturn(Flux.empty());
    webClient
        .get()
        .uri("/api/rest/v1/issue?reportedBy=" + reporter)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(Issue.class)
        .value(
            issues -> {
              assertEquals(1, issues.size());
              assertEquals(issueId, issues.get(0).getId());
            });

    verify(issueRepository).findAllByReporter(anyString());
    verify(issueCommentRepository).findAllByIssueId(anyString());
    verify(userRepository, times(2)).findById(anyString());
    verify(userRepository).exists(anyString());
    verify(projectRepository).findById(anyString());
    verify(projectVersionRepository).findAll(anyString());
  }

  @Test
  void test_GetAllByProjectId_WithInValidReporter() {

    var reporter = UUID.randomUUID().toString();
    var invalidReporterException = IssueException.invalidUser(reporter);
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    webClient
        .get()
        .uri("/api/rest/v1/issue?reportedBy=" + reporter)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo("/api/rest/v1/issue")
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidReporterException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(userRepository).exists(anyString());
  }

  @Test
  void test_Get_WithValidIssueId() {
    var issueId1 = UUID.randomUUID().toString();

    var userEntity = new UserEntity();
    userEntity.setId(UUID.randomUUID().toString());
    userEntity.setEmail("test@test.com");
    userEntity.setEnabled(true);
    userEntity.setLastName("lName");
    userEntity.setFirstName("fName");
    userEntity.setAccess(1);

    var projectEntity = new ProjectEntity();
    projectEntity.setCreatedAt(LocalDateTime.now());
    projectEntity.setStatus(1);
    projectEntity.setName("TestProject");
    projectEntity.setDescription("DESC");
    projectEntity.setAuthor(userEntity.getId());
    projectEntity.setId(UUID.randomUUID().toString());

    var projectVersionEntity = new ProjectVersionEntity();
    projectVersionEntity.setId(UUID.randomUUID().toString());
    projectVersionEntity.setVersion("v1.0");

    var projectVersion = new ProjectVersion(projectVersionEntity);

    var projectInfo = new ProjectInfo();
    projectInfo.setProjectId(projectEntity.getId());
    projectInfo.setVersionId(projectVersionEntity.getId());

    var user = new User(userEntity);

    var project = new Project(projectEntity, user, Set.of(projectVersion));

    var issue1 = new IssueEntity();
    issue1.setId(issueId1);
    issue1.setProjects(Set.of(new ProjectInfoRef(projectInfo)));
    issue1.setAssignee(userEntity.getId());
    issue1.setPriority(1);
    issue1.setSeverity(1);

    issue1.setProjects(Set.of(new ProjectInfoRef(projectInfo)));
    issue1.setHeader("Summary for test issue");
    issue1.setDescription("Test issue");
    issue1.setReporter(UUID.randomUUID().toString());

    var expectedIssue =
        Issue.builder(issue1)
            .assignee(user)
            .watchers(Set.of())
            .comments(List.of())
            .reporter(user)
            .projects(List.of(project))
            .build();

    var issueEntityMono = Mono.just(issue1);

    when(userRepository.findById(anyString())).thenReturn(Mono.just(userEntity));
    when(projectRepository.findById(anyString())).thenReturn(Mono.just(projectEntity));
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.just(projectVersionEntity));
    when(issueRepository.findById(issueId1)).thenReturn(issueEntityMono);
    when(issueCommentRepository.findAllByIssueId(anyString())).thenReturn(Flux.empty());
    webClient
        .get()
        .uri("/api/rest/v1/issue/" + issueId1)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Issue.class)
        .value(issue -> assertEquals(expectedIssue, issue));

    verify(issueRepository).findById(anyString());
    verify(issueCommentRepository).findAllByIssueId(anyString());
    verify(userRepository, times(3)).findById(anyString());
    verify(projectRepository).findById(anyString());
    verify(projectVersionRepository).findAll(anyString());
  }

  @Test
  void test_Get_WithInvalidIssue() {
    var issueId1 = UUID.randomUUID().toString();

    var issueNotFoundException = new IssueNotFoundException(issueId1);

    when(issueRepository.findById(issueId1)).thenReturn(Mono.error(() -> issueNotFoundException));

    var restPath = "/api/rest/v1/issue/" + issueId1;

    webClient
        .get()
        .uri(restPath)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(404)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(issueNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).findById(anyString());
  }

  @Test
  void test_Create_WithValidIssue() {

    var userEntity = new UserEntity();
    userEntity.setId(UUID.randomUUID().toString());
    userEntity.setEmail("test@test.com");
    userEntity.setEnabled(true);
    userEntity.setLastName("lName");
    userEntity.setFirstName("fName");
    userEntity.setAccess(1);

    var projectEntity = new ProjectEntity();
    projectEntity.setCreatedAt(LocalDateTime.now());
    projectEntity.setStatus(1);
    projectEntity.setName("TestProject");
    projectEntity.setDescription("DESC");
    projectEntity.setAuthor(userEntity.getId());
    projectEntity.setId(UUID.randomUUID().toString());

    var projectVersionEntity = new ProjectVersionEntity();
    projectVersionEntity.setId(UUID.randomUUID().toString());
    projectVersionEntity.setVersion("v1.0");

    var projectInfo = new ProjectInfo();
    projectInfo.setProjectId(UUID.randomUUID().toString());
    projectInfo.setVersionId(UUID.randomUUID().toString());

    IssueRequest issueRequest = new IssueRequest();
    issueRequest.setProjects(Set.of());
    issueRequest.setHeader("This is for test only");
    issueRequest.setPriority(Priority.LOW);
    issueRequest.setDescription("Test desc");
    issueRequest.setBusinessUnit("test");
    issueRequest.setSeverity(Severity.HIGH);
    issueRequest.setProjects(Set.of(projectInfo));
    issueRequest.setReporter(UUID.randomUUID().toString());

    var issueEntity = new IssueEntity(issueRequest);

    when(userRepository.findById(anyString())).thenReturn(Mono.just(userEntity));
    when(projectRepository.findById(anyString())).thenReturn(Mono.just(projectEntity));
    when(projectVersionRepository.exists(anyString(), anyString())).thenReturn(Mono.just(true));
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.just(projectVersionEntity));
    when(issueCommentRepository.findAllByIssueId(anyString())).thenReturn(Flux.empty());
    when(projectRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueRepository.save(any(IssueEntity.class))).thenReturn(Mono.just(issueEntity));

    webClient
        .post()
        .uri("/api/rest/v1/issue")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueRequest), IssueRequest.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Issue.class);

    verify(issueRepository).save(any(IssueEntity.class));
    verify(issueCommentRepository).findAllByIssueId(anyString());
    verify(userRepository, times(2)).findById(anyString());
    verify(userRepository).exists(anyString());
    verify(projectRepository).findById(anyString());
    verify(projectRepository).exists(anyString());
    verify(projectVersionRepository).exists(anyString(), anyString());
    verify(projectVersionRepository).findAll(anyString());
  }

  @Test
  void test_Assign_Valid() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);

    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueRepository.assign(anyString(), anyString())).thenReturn(Mono.just(true));

    webClient
        .patch()
        .uri("/api/rest/v1/issue/" + issueId + "/assign")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
    verify(issueRepository).assign(anyString(), anyString());
  }

  @Test
  void test_Assign_InValid_IssueId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var issueNotFoundException = new IssueNotFoundException(issueId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(false));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/assign";
    webClient
        .patch()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(issueNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_Assign_InValid_UserId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var userNotFoundException = new UserNotFoundException(userId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/assign";
    webClient
        .patch()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(userNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_UnAssign_Valid() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);

    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueRepository.unassign(anyString(), anyString())).thenReturn(Mono.just(true));

    webClient
        .method(HttpMethod.DELETE)
        .uri("/api/rest/v1/issue/" + issueId + "/assign")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
    verify(issueRepository).unassign(anyString(), anyString());
  }

  @Test
  void test_UnAssign_InValid_IssueId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var issueNotFoundException = new IssueNotFoundException(issueId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(false));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/assign";
    webClient
        .method(HttpMethod.DELETE)
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(issueNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_UnAssign_InValid_UserId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var userNotFoundException = new UserNotFoundException(userId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/assign";
    webClient
        .method(HttpMethod.DELETE)
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(userNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_AddWatcher_Valid() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);

    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueRepository.addWatcher(anyString(), anyString())).thenReturn(Mono.just(true));

    webClient
        .patch()
        .uri("/api/rest/v1/issue/" + issueId + "/watcher")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
    verify(issueRepository).addWatcher(anyString(), anyString());
  }

  @Test
  void test_AddWatcher_InValid_IssueId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var issueNotFoundException = new IssueNotFoundException(issueId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(false));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/watcher";
    webClient
        .patch()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(issueNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_AddWatcher_InValid_UserId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var userNotFoundException = new UserNotFoundException(userId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/watcher";
    webClient
        .patch()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(userNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_RemoveWatcher_Valid() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);

    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueRepository.removeWatcher(anyString(), anyString())).thenReturn(Mono.just(true));

    webClient
        .method(HttpMethod.DELETE)
        .uri("/api/rest/v1/issue/" + issueId + "/watcher")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectStatus()
        .isNoContent();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
    verify(issueRepository).removeWatcher(anyString(), anyString());
  }

  @Test
  void test_RemoveWatcher_InValid_IssueId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var issueNotFoundException = new IssueNotFoundException(issueId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(false));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/watcher";
    webClient
        .method(HttpMethod.DELETE)
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(issueNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_RemoveWatcher_InValid_UserId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var issueAssignRequest = new IssueAssignRequest();
    issueAssignRequest.setUser(userId);
    var userNotFoundException = new UserNotFoundException(userId);
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    var path = "/api/rest/v1/issue/" + issueId + "/watcher";
    webClient
        .method(HttpMethod.DELETE)
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueAssignRequest), IssueAssignRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(userNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(issueRepository).exists(anyString());
    verify(userRepository).exists(anyString());
  }

  @Test
  void test_AddComment_Valid() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var path = "/api/rest/v1/issue/" + issueId + "/comment";

    var issueCommentRequest = new IssueCommentRequest();
    issueCommentRequest.setContent("Test comment");
    issueCommentRequest.setUserId(userId);
    var issueCommentEntity = new IssueCommentEntity(issueCommentRequest);

    var userEntity = new UserEntity();
    userEntity.setId(userId);
    userEntity.setEnabled(true);
    userEntity.setAccess(0);
    userEntity.setEmail("test@test.com");
    userEntity.setFirstName("fName");
    userEntity.setFirstName("lName");
    userEntity.setPassword("********");

    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(userRepository.findById(anyString())).thenReturn(Mono.just(userEntity));
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueCommentRepository.save(anyString(), any())).thenReturn(Mono.just(issueCommentEntity));

    webClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueCommentRequest), IssueCommentRequest.class)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(IssueComment.class);
    verify(userRepository).exists(anyString());
    verify(userRepository).findById(anyString());
    verify(issueRepository).exists(anyString());
    verify(issueCommentRepository).save(anyString(), any());
  }

  @Test
  void test_AddComment_InValid_UserId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var path = "/api/rest/v1/issue/" + issueId + "/comment";

    var issueCommentRequest = new IssueCommentRequest();
    issueCommentRequest.setContent("Test comment");
    issueCommentRequest.setUserId(userId);

    var userNotFoundException = new UserNotFoundException(userId);

    when(userRepository.exists(anyString())).thenReturn(Mono.just(false));
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(true));

    webClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueCommentRequest), IssueCommentRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(userNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(userRepository).exists(anyString());
    verify(issueRepository).exists(anyString());
  }

  @Test
  void test_AddComment_InValid_IssueId() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var path = "/api/rest/v1/issue/" + issueId + "/comment";

    var issueCommentRequest = new IssueCommentRequest();
    issueCommentRequest.setContent("Test comment");
    issueCommentRequest.setUserId(userId);

    var issueNotFoundException = new IssueNotFoundException(issueId);

    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(false));

    webClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueCommentRequest), IssueCommentRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(issueNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(userRepository).exists(anyString());
    verify(issueRepository).exists(anyString());
  }

  @Test
  void test_AddComment_InValid_Comment() {
    var issueId = UUID.randomUUID().toString();
    var userId = UUID.randomUUID().toString();
    var path = "/api/rest/v1/issue/" + issueId + "/comment";

    var issueCommentRequest = new IssueCommentRequest();
    issueCommentRequest.setContent("");
    issueCommentRequest.setUserId(userId);

    var invalidCommentException = IssueException.invalidComment(issueCommentRequest.getContent());

    when(userRepository.exists(anyString())).thenReturn(Mono.just(true));
    when(issueRepository.exists(anyString())).thenReturn(Mono.just(false));

    webClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(issueCommentRequest), IssueCommentRequest.class)
        .exchange()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidCommentException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(userRepository).exists(anyString());
    verify(issueRepository).exists(anyString());
  }

  @Test
  void test_Done() {

    var issueId = UUID.randomUUID().toString();
    var path = "/api/rest/v1/issue/" + issueId + "/done";
    when(issueRepository.done(anyString())).thenReturn(Mono.just(true));
    webClient.delete().uri(path).exchange().expectStatus().isNoContent();
    verify(issueRepository).done(anyString());
  }
}
