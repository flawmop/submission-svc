package com.insilicosoft.portal.svc.submission.controller;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.insilicosoft.portal.svc.submission.SubmissionIdentifiers;

import dasniko.testcontainers.keycloak.KeycloakContainer;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class SubmissionControllerE2E {

  private static final HttpHeaders httpHeaders = new HttpHeaders();
  private static final MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
  private static final MediaType textWithCharset = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);
  private static final String message = "No Multipart file! Did you supply the parameter '" + SubmissionIdentifiers.PARAM_NAME_SIMULATION_FILE + "' in the POST request?";
  private static final String postUrl = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION.concat(SubmissionIdentifiers.REQUEST_MAPPING_SIMULATION);
  private static final String goodRequestFileName = "request_good.json";
  private static final Path goodPath = Path.of("src",  "test", "resources", "requests", goodRequestFileName);

  private static KeycloakToken bjornTokens;

  {
    httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
  }

  @LocalServerPort
  private int port;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private WebTestClient webTestClient;

  // Alternatively localhost:5000/keycloak:19.0
  @Container
  private static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:19.0")
                                                                         .withRealmImportFile("keycloak/test-realm-config.json");

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                 () -> keycloak.getAuthServerUrl() + "realms/PolarBookshop");
  }

  @AfterEach
  void afterEach() {
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "simulation")).isEqualTo(0);
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "simulation_message")).isEqualTo(0);
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "simulation_plasmapoints")).isEqualTo(0);
    assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "submission")).isEqualTo(0);
  }

  @BeforeAll
  static void generateAccessTokens() {
    WebClient webClient = WebClient.builder().baseUrl(keycloak.getAuthServerUrl() + "realms/PolarBookshop/protocol/openid-connect/token")
                                             .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                             .build();
    bjornTokens = authenticateWith("bjorn", "password", webClient);
  }

  @DisplayName("Test GET method(s)")
  @Nested
  class GetMethods {
    @DisplayName("Fail on Submission not found")
    @Test
    void failOnSubmissionNotFound() {
      var submissionId = 1l;
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/" + String.valueOf(submissionId);

      webTestClient.get()
                   .uri(url)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                   .exchange()
                   .expectStatus().isNotFound()
                   .expectHeader().contentType(textWithCharset)
                   .expectBody(String.class).isEqualTo("Submission with identifier '1' was not found");
    }
  }

  @DisplayName("Test DELETE method(s)")
  @Nested
  class DeleteMethods {
    @DisplayName("Fail on Submission not found")
    @Test
    void failOnSubmissionNotFound() {
      var submissionId = 1l;
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/{id}";

      webTestClient.delete()
                   .uri(url, submissionId)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                   .exchange()
                   .expectStatus().isNotFound()
                   .expectHeader().contentType(textWithCharset)
                   .expectBody(String.class).isEqualTo("Submission with identifier '1' was not found");
    }
  }

  @DisplayName("Test POST method(s)")
  @Nested
  class PostMethods {
    @DisplayName("Fail on unauthorized")
    @Test
    void failOnUnauthorized() {
      webTestClient.post()
                   .uri(postUrl)
                   .headers(headers -> {
                     headers.addAll(httpHeaders);
                   })
                   .exchange()
                   .expectStatus().isUnauthorized();
    }

    @DisplayName("Fail on multipart exception")
    @Test
    void failOnMultipartException() {
      webTestClient.post()
                   .uri(postUrl)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                     headers.addAll(httpHeaders);
                   })
                  .exchange()
                  .expectStatus().is5xxServerError()
                  .expectBody(String.class).isEqualTo("Error occurred during file upload - MultipartException");
    }

    @DisplayName("Fail on expected request param not supplied")
    @Test
    void failOnBadParamName() {
      var multipartBodyBuilder = new MultipartBodyBuilder();
      multipartBodyBuilder.part("fish",  new FileSystemResource(goodPath));
      webTestClient.post()
                   .uri(postUrl)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                  .body(fromMultipartData(multipartBodyBuilder.build()))
                  .exchange()
                  .expectStatus().isBadRequest()
                  .expectBody(String.class).isEqualTo(message);
    }
  }

  @DisplayName("Test Submission lifecycle")
  @Nested
  class SubmissionLifecycle {
    @DisplayName("Success on Submission lifecycle")
    @Test
    void successOnSubmissionLifecycle() {
      var submissionId = 1l;
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/{id}";
      var multipartBodyBuilder = new MultipartBodyBuilder();
      multipartBodyBuilder.part(SubmissionIdentifiers.PARAM_NAME_SIMULATION_FILE,
                                new FileSystemResource(goodPath));

      webTestClient.post()
                   .uri(postUrl)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                  .body(fromMultipartData(multipartBodyBuilder.build()))
                  .exchange()
                  .expectStatus().isCreated()
                  .expectHeader().location("http://localhost:" + port + postUrl + "/" + submissionId)
                  .expectBody().isEmpty();

      webTestClient.get()
                   .uri(url, submissionId)
                   .accept(MediaType.APPLICATION_JSON)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                   .exchange()
                   .expectStatus().isOk()
                   .expectHeader().contentType(applicationJson)
                   .expectBody().jsonPath("$.entityId").isEqualTo(submissionId);

      webTestClient.delete()
                   .uri(url, submissionId)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                   .exchange()
                   .expectStatus().isNoContent()
                   .expectBody().isEmpty();
    }
  }

  private static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
    return webClient.post()
                    .body(fromFormData("grant_type", "password").with("client_id", "polar-test")
                                                                .with("username", username)
                                                                .with("password", password))
                    .retrieve()
                    .bodyToMono(KeycloakToken.class)
                    .block();
  }

  private record KeycloakToken(String accessToken) {
    @JsonCreator
    private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
      this.accessToken = accessToken;
    }
  }
}