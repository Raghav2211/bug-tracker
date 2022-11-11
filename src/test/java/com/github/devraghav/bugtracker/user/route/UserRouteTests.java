package com.github.devraghav.bugtracker.user.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.github.devraghav.bugtracker.user.dto.AccessLevel;
import com.github.devraghav.bugtracker.user.dto.User;
import com.github.devraghav.bugtracker.user.dto.UserException;
import com.github.devraghav.bugtracker.user.dto.UserRequest;
import com.github.devraghav.bugtracker.user.entity.UserEntity;
import com.github.devraghav.bugtracker.user.repository.UserAlreadyExistsException;
import com.github.devraghav.bugtracker.user.repository.UserNotFoundException;
import com.github.devraghav.bugtracker.user.repository.UserRepository;
import com.github.devraghav.bugtracker.user.service.UserService;
import java.util.UUID;
import org.hamcrest.Matchers;
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
    classes = {UserRouteDefinition.class, UserRouteHandler.class, UserService.class})
@WebFluxTest
@AutoConfigureWebClient
public class UserRouteTests {

  @Autowired private WebTestClient webClient;
  @MockBean private UserRepository userRepository;

  @Test
  void testGetAll() {

    var userId = UUID.randomUUID().toString();

    var userEntity1 = getUserEntity(userId);

    var userEntityFlux = Flux.just(userEntity1);
    when(userRepository.findAll()).thenReturn(userEntityFlux);
    webClient
        .get()
        .uri("/api/rest/v1/user")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBodyList(User.class)
        .value(
            users -> {
              assertEquals(1, users.size());
              assertEquals(userId, users.get(0).getId());
            });
    verify(userRepository).findAll();
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void testGetWithValidUser() {
    var userId = UUID.randomUUID().toString();

    var userEntity = getUserEntity(userId);

    var expectedUser = new User();
    expectedUser.setId(userId);
    expectedUser.setEnabled(true);
    expectedUser.setAccessLevel(AccessLevel.ADMIN);
    expectedUser.setEmail("test@test.com");
    expectedUser.setFirstName("fName");
    expectedUser.setFirstName("lName");

    var userEntityMono = Mono.just(userEntity);
    when(userRepository.findById(userId)).thenReturn(userEntityMono);

    webClient
        .get()
        .uri("/api/rest/v1/user/" + userId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(User.class)
        .value(
            user -> {
              assertEquals(expectedUser, user);
            });
    verify(userRepository).findById(anyString());
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void testGetWithInvalidUser() {
    var userId = UUID.randomUUID().toString();

    var userNotFoundException = new UserNotFoundException(userId);

    when(userRepository.findById(userId)).thenReturn(Mono.error(() -> userNotFoundException));

    var findByIdRestPath = "/api/rest/v1/user/" + userId;

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
        .isEqualTo(userNotFoundException.getMessage())
        .jsonPath("$.meta")
        .value(Matchers.hasEntry("userId", userId))
        .jsonPath("$.timeStamp")
        .exists();
    verify(userRepository).findById(anyString());
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void testCreateWithValidUser() {
    var userRequest = getUserRequest();

    var userEntity = new UserEntity(userRequest);

    when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(userEntity));

    webClient
        .post()
        .uri("/api/rest/v1/user")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(userRequest), UserRequest.class)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(User.class);
    verify(userRepository).save(any(UserEntity.class));
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void testCreateWithInValidEmail() {
    var userRequest = getUserRequest();
    // set invlaid email
    userRequest.setEmail("abc");

    var invalidEmailException = UserException.invalidEmail(userRequest.getEmail());
    var path = "/api/rest/v1/user";

    webClient
        .post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(userRequest), UserRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(path)
        .jsonPath("$.errorMessage")
        .isEqualTo(invalidEmailException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verifyNoInteractions(userRepository);
  }

  @Test
  void testCreateWithDuplicateUser() {

    var userRequest = getUserRequest();

    var duplicateUserException = UserAlreadyExistsException.withEmail(userRequest.getEmail());
    when(userRepository.save(any(UserEntity.class)))
        .thenReturn(Mono.error(() -> duplicateUserException));

    var restPath = "/api/rest/v1/user";

    webClient
        .post()
        .uri(restPath)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(userRequest), UserRequest.class)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo(400)
        .jsonPath("$.path")
        .isEqualTo(restPath)
        .jsonPath("$.errorMessage")
        .isEqualTo(duplicateUserException.getMessage())
        .jsonPath("$.timeStamp")
        .exists();
    verify(userRepository).save(any(UserEntity.class));
    verifyNoMoreInteractions(userRepository);
  }

  private UserRequest getUserRequest() {
    var userRequest = new UserRequest();
    userRequest.setEmail("test@test.com");
    userRequest.setFirstName("fName");
    userRequest.setLastName("lName");
    userRequest.setPassword("********");
    return userRequest;
  }

  private UserEntity getUserEntity(String userId) {
    var userEntity = new UserEntity();
    userEntity.setId(userId);
    userEntity.setEnabled(true);
    userEntity.setAccess(0);
    userEntity.setEmail("test@test.com");
    userEntity.setFirstName("fName");
    userEntity.setFirstName("lName");
    userEntity.setPassword("********");
    return userEntity;
  }
}
