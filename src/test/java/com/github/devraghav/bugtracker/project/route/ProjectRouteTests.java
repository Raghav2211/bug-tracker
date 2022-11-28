package com.github.devraghav.bugtracker.project.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.github.devraghav.bugtracker.project.dto.*;
import com.github.devraghav.bugtracker.project.entity.ProjectEntity;
import com.github.devraghav.bugtracker.project.entity.ProjectVersionEntity;
import com.github.devraghav.bugtracker.project.repository.ProjectAlreadyExistsException;
import com.github.devraghav.bugtracker.project.repository.ProjectNotFoundException;
import com.github.devraghav.bugtracker.project.repository.ProjectRepository;
import com.github.devraghav.bugtracker.project.repository.ProjectVersionRepository;
import com.github.devraghav.bugtracker.project.service.ProjectService;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import com.github.devraghav.bugtracker.user.service.UserService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ContextConfiguration(
    classes = {
      ProjectRouteDefinition.class,
      ProjectRouteHandler.class,
      ProjectService.class,
      UserService.class
    })
@WebFluxTest
@AutoConfigureWebClient
public class ProjectRouteTests {

  @Autowired private WebTestClient webClient;
  @MockBean private ProjectRepository projectRepository;
  @MockBean private ProjectVersionRepository projectVersionRepository;
  @MockBean private UserRepository userRepository;

  @Test
  void testGetAll() {

    var projectId = UUID.randomUUID().toString();
    var authorId = UUID.randomUUID().toString();

    var userEntity = getUserEntity(authorId);

    var projectEntity = new ProjectEntity();
    projectEntity.setId(projectId);
    projectEntity.setEnabled(true);
    projectEntity.setDescription("Test project");
    projectEntity.setName("TestProject");
    projectEntity.setStatus(1);
    projectEntity.setAuthor(userEntity.getId());
    projectEntity.setCreatedAt(LocalDateTime.now());

    var userMono = Mono.just(userEntity);
    var projectEntityFlux = Flux.just(projectEntity);

    when(userRepository.findById(anyString())).thenReturn(userMono);
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.empty());
    when(projectRepository.findAll()).thenReturn(projectEntityFlux);
    webClient
        .get()
        .uri("/api/rest/v1/project")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(Project.class)
        .value(
            projects -> {
              assertEquals(1, projects.size());
              assertEquals(projectId, projects.get(0).getId());
              assertEquals(authorId, projects.get(0).getAuthor().getId());
            });

    verify(userRepository).findById(anyString());
    verify(projectVersionRepository).findAll(anyString());
    verify(projectRepository).findAll();
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testGetAllEmpty() {

    when(projectRepository.findAll()).thenReturn(Flux.empty());
    webClient
        .get()
        .uri("/api/rest/v1/project")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$")
        .isEmpty();

    verify(projectRepository).findAll();
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testGetWithValidProject() {
    var projectId1 = UUID.randomUUID().toString();
    var authorId = UUID.randomUUID().toString();

    var userEntity = getUserEntity(authorId);

    var projectEntity = new ProjectEntity();
    projectEntity.setId(projectId1);
    projectEntity.setEnabled(true);
    projectEntity.setDescription("Test project");
    projectEntity.setName("TestProject");
    projectEntity.setStatus(1);
    projectEntity.setAuthor(authorId);
    projectEntity.setCreatedAt(LocalDateTime.now());

    var expectedProject = new Project(projectEntity, new User(userEntity));

    var userMono = Mono.just(userEntity);
    var projectEntityMono = Mono.just(projectEntity);

    when(userRepository.findById(anyString())).thenReturn(userMono);
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.empty());
    when(projectRepository.findById(projectId1)).thenReturn(projectEntityMono);

    webClient
        .get()
        .uri("/api/rest/v1/project/" + projectId1)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Project.class)
        .value(
            project -> {
              assertEquals(expectedProject, project);
            });

    verify(userRepository).findById(anyString());
    verify(projectVersionRepository).findAll(anyString());
    verify(projectRepository).findById(anyString());
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testGetWithInvalidProject() {
    var projectId1 = UUID.randomUUID().toString();

    var projectNotFoundException = new ProjectNotFoundException(projectId1);

    when(projectRepository.findById(projectId1))
        .thenReturn(Mono.error(() -> projectNotFoundException));

    var findByIdRestPath = "/api/rest/v1/project/" + projectId1;

    webClient
        .get()
        .uri(findByIdRestPath)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(404)
        .jsonPath("$.path")
        .isEqualTo(findByIdRestPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(projectNotFoundException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(projectRepository).findById(anyString());
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateWithValidProject() {
    var authorId = UUID.randomUUID().toString();

    var projectRequest = new ProjectRequest();
    projectRequest.setDescription("Test project");
    projectRequest.setStatus(ProjectStatus.IN_PROGRESS);
    projectRequest.setName("Testproject");
    projectRequest.setAuthor(authorId);

    var userEntity = getUserEntity(authorId);

    var projectEntity = new ProjectEntity(projectRequest);

    var userMono = Mono.just(userEntity);

    when(userRepository.findById(anyString())).thenReturn(userMono);
    when(projectVersionRepository.findAll(anyString())).thenReturn(Flux.empty());
    when(projectRepository.save(any(ProjectEntity.class))).thenReturn(Mono.just(projectEntity));

    webClient
        .post()
        .uri("/api/rest/v1/project")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectRequest), ProjectRequest.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Project.class);

    verify(userRepository, times(2)).findById(anyString());
    verify(projectVersionRepository).findAll(anyString());
    verify(projectRepository).save(any(ProjectEntity.class));
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateWithEmptyProjectName() {

    var projectRequest = new ProjectRequest();
    projectRequest.setDescription("Test project");
    projectRequest.setStatus(ProjectStatus.IN_PROGRESS);
    projectRequest.setName("");
    var restPath = "/api/rest/v1/project";

    var invalidProjectNameException = ProjectException.invalidName(projectRequest.getName());

    webClient
        .post()
        .uri(restPath)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectRequest), ProjectRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidProjectNameException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateWithProjectDescriptionLengthGreaterThan20() {

    var projectRequest = new ProjectRequest();
    projectRequest.setDescription(
        "Test project Test project Test project Test project Test project Test project");
    projectRequest.setStatus(ProjectStatus.IN_PROGRESS);
    projectRequest.setName("Testproject");
    var restPath = "/api/rest/v1/project";

    var invalidProjectNameException =
        ProjectException.invalidDescription(projectRequest.getDescription());

    webClient
        .post()
        .uri(restPath)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectRequest), ProjectRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidProjectNameException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateWithDuplicateProject() {

    var authorId = UUID.randomUUID().toString();

    var projectRequest = new ProjectRequest();
    projectRequest.setDescription("Test project");
    projectRequest.setStatus(ProjectStatus.IN_PROGRESS);
    projectRequest.setName("Testproject");
    projectRequest.setAuthor(authorId);

    var userEntity = getUserEntity(authorId);

    var userMono = Mono.just(userEntity);

    when(userRepository.findById(anyString())).thenReturn(userMono);

    var duplicateProjectException =
        ProjectAlreadyExistsException.withName(projectRequest.getName());
    when(projectRepository.save(any(ProjectEntity.class)))
        .thenReturn(Mono.error(() -> duplicateProjectException));

    var restPath = "/api/rest/v1/project";

    webClient
        .post()
        .uri(restPath)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectRequest), ProjectRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(duplicateProjectException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();

    verify(userRepository).findById(anyString());
    verify(projectRepository).save(any(ProjectEntity.class));
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateWithInvalidAuthor() {

    var authorId = UUID.randomUUID().toString();

    var projectRequest = new ProjectRequest();
    projectRequest.setDescription("Test project");
    projectRequest.setStatus(ProjectStatus.IN_PROGRESS);
    projectRequest.setName("Testproject");
    projectRequest.setAuthor(authorId);

    var userNotFoundException = new UserNotFoundException(authorId);

    var invalidAuthorException = ProjectException.authorNotFound(authorId);

    when(userRepository.findById(anyString())).thenReturn(Mono.error(() -> userNotFoundException));

    var restPath = "/api/rest/v1/project";

    webClient
        .post()
        .uri(restPath)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectRequest), ProjectRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidAuthorException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(userRepository).findById(anyString());
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateWithoutAuthor() {

    var projectRequest = new ProjectRequest();
    projectRequest.setDescription("Test project");
    projectRequest.setStatus(ProjectStatus.IN_PROGRESS);
    projectRequest.setName("Testproject");

    var noAuthorException = ProjectException.nullAuthor();

    var restPath = "/api/rest/v1/project";

    webClient
        .post()
        .uri(restPath)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectRequest), ProjectRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(noAuthorException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateWithInvalidAuthorAccess() {

    var authorId = UUID.randomUUID().toString();

    var projectRequest = new ProjectRequest();
    projectRequest.setDescription("Test project");
    projectRequest.setStatus(ProjectStatus.IN_PROGRESS);
    projectRequest.setName("Testproject");
    projectRequest.setAuthor(authorId);

    var userEntity = getUserEntity(authorId);
    // set READ access
    userEntity.setAccess(1);

    var invalidAuthorAccessException = ProjectException.authorNotHaveWriteAccess(authorId);

    when(userRepository.findById(anyString())).thenReturn(Mono.just(userEntity));

    var restPath = "/api/rest/v1/project";

    webClient
        .post()
        .uri(restPath)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectRequest), ProjectRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidAuthorAccessException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(userRepository).findById(anyString());
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateProjectVersionWithValidProjectId() {
    var projectId = UUID.randomUUID().toString();

    var projectVersionRequest = new ProjectVersionRequest();
    projectVersionRequest.setVersion("v1.0");

    var projectVersionEntity = new ProjectVersionEntity(projectVersionRequest);

    when(projectRepository.exists(projectId)).thenReturn(Mono.just(true));

    when(projectVersionRepository.save(anyString(), any(ProjectVersionEntity.class)))
        .thenReturn(Mono.just(projectVersionEntity));

    webClient
        .post()
        .uri("/api/rest/v1/project/" + projectId + "/version")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectVersionRequest), ProjectVersionRequest.class)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(ProjectVersion.class);
    verify(projectRepository).exists(anyString());
    verify(projectVersionRepository).save(anyString(), any(ProjectVersionEntity.class));
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  @Test
  void testCreateProjectVersionWithInValidProjectId() {
    var projectId = UUID.randomUUID().toString();

    var projectVersionRequest = new ProjectVersionRequest();
    projectVersionRequest.setVersion("v1.0");

    var invalidProjectException = ProjectException.projectNotFound(projectId);

    when(projectRepository.exists(projectId)).thenReturn(Mono.just(false));

    var path = "/api/rest/v1/project/" + projectId + "/version";
    webClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(projectVersionRequest), ProjectVersionRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidProjectException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(projectRepository).exists(anyString());
    verifyNoMoreInteractions(userRepository, projectVersionRepository, projectRepository);
  }

  private UserEntity getUserEntity(String userId) {
    var userEntity = new UserEntity();
    userEntity.setId(userId);
    userEntity.setFirstName("fName");
    userEntity.setLastName("lName");
    userEntity.setEmail("test@test.com");
    userEntity.setEnabled(true);
    userEntity.setAccess(0);
    return userEntity;
  }
}
