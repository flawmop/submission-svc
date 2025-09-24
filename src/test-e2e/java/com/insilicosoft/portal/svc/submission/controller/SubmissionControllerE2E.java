package com.insilicosoft.portal.svc.submission.controller;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
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

@AutoConfigureRestDocs
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
  private RestDocumentationContextProvider restDocumentation;
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

  // TODO: Remove assertions that test for residual data in db!
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
                   .expectAll(
                     rsc -> rsc.expectStatus().isNotFound(),
                     rsc -> rsc.expectHeader().contentType(textWithCharset),
                     rsc -> rsc.expectBody(String.class).isEqualTo("Submission with/using identifier '1' was not found"));
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
                   .expectAll(
                     rsc -> rsc.expectStatus().isNotFound(),
                     rsc -> rsc.expectHeader().contentType(textWithCharset),
                     rsc -> rsc.expectBody(String.class).isEqualTo("Submission with/using identifier '1' was not found"));
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
                  .expectAll(
                    rsc -> rsc.expectStatus().is5xxServerError(),
                    rsc -> rsc.expectBody(String.class).isEqualTo("Error occurred during file upload - MultipartException"));
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
                  .expectAll(
                    rsc -> rsc.expectStatus().isBadRequest(),
                    rsc -> rsc.expectBody(String.class).isEqualTo(message));
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

      webTestClient.mutate()
                   .filter(documentationConfiguration(restDocumentation).operationPreprocessors()
                                                                        .withRequestDefaults(prettyPrint(),
                                                                                             modifyHeaders().remove(HttpHeaders.ACCEPT_ENCODING),
                                                                                             modifyHeaders().remove(HttpHeaders.ACCEPT),
                                                                                             modifyHeaders().set(HttpHeaders.USER_AGENT, "..."),
                                                                                             modifyHeaders().set(HttpHeaders.HOST, "..."),
                                                                                             modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer ..."))
                                                                        .withResponseDefaults(modifyHeaders().remove(HttpHeaders.CACHE_CONTROL),
                                                                                              modifyHeaders().remove(HttpHeaders.PRAGMA),
                                                                                              modifyHeaders().remove(HttpHeaders.EXPIRES),
                                                                                              modifyHeaders().remove(HttpHeaders.DATE),
                                                                                              modifyHeaders().remove("X-Content-Type-Options"),
                                                                                              modifyHeaders().remove("X-Frame-Options"),
                                                                                              modifyHeaders().remove("X-XSS-Protection")))
                   .build()
                   .post()
                   .uri(postUrl)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                  .contentType(MediaType.MULTIPART_FORM_DATA)
                  .body(fromMultipartData(multipartBodyBuilder.build()))
                  .exchange()
                  .expectAll(
                    rsc -> rsc.expectStatus().isCreated(),
                    rsc -> rsc.expectHeader().location("http://localhost:" + port + postUrl + "/" + submissionId),
                    rsc -> rsc.expectBody().isEmpty())
                  .expectBody(Void.class)
                  .consumeWith(document("submission-post",
                                        requestParts(partWithName(SubmissionIdentifiers.PARAM_NAME_SIMULATION_FILE).description("IC50 data file")),
                                        responseHeaders(headerWithName(HttpHeaders.LOCATION).description("URL where you can find the new submission"))));

      webTestClient.mutate()
                   .filter(documentationConfiguration(restDocumentation).operationPreprocessors()
                                                                        .withRequestDefaults(prettyPrint(),
                                                                                             modifyHeaders().remove(HttpHeaders.ACCEPT_ENCODING),
                                                                                             modifyHeaders().set(HttpHeaders.USER_AGENT, "..."),
                                                                                             modifyHeaders().set(HttpHeaders.HOST, "..."),
                                                                                             modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer ..."))
                                                                        .withResponseDefaults(prettyPrint()))
                   .build()
                   .get()
                   .uri(url, submissionId)
                   .accept(MediaType.APPLICATION_JSON)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                   .exchange()
                   .expectAll(
                     rsc -> rsc.expectStatus().isOk(),
                     rsc -> rsc.expectHeader().contentType(applicationJson),
                     rsc -> rsc.expectBody().jsonPath("$.submissionId").isEqualTo(submissionId)
                               .consumeWith(document("submission-get",
                                                     pathParameters(parameterWithName("id").description("Submission ID")),
                                                     responseFields(fieldWithPath("submissionId").description("Submission ID"),
                                                                    fieldWithPath("state").description("Submission state. Values are `APPROVED`, `COMPLETED`, `CREATED`, `FAILED`, `REJECTED`")))));

      webTestClient.mutate()
                   .filter(documentationConfiguration(restDocumentation).operationPreprocessors()
                                                                        .withRequestDefaults(modifyHeaders().remove(HttpHeaders.ACCEPT_ENCODING),
                                                                                             modifyHeaders().remove(HttpHeaders.ACCEPT),
                                                                                             modifyHeaders().set(HttpHeaders.USER_AGENT, "..."),
                                                                                             modifyHeaders().set(HttpHeaders.HOST, "..."),
                                                                                             modifyHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer ..."))
                                                                        .withResponseDefaults(modifyHeaders().remove(HttpHeaders.CACHE_CONTROL),
                                                                                              modifyHeaders().remove(HttpHeaders.PRAGMA),
                                                                                              modifyHeaders().remove(HttpHeaders.EXPIRES),
                                                                                              modifyHeaders().remove(HttpHeaders.DATE),
                                                                                              modifyHeaders().remove("X-Content-Type-Options"),
                                                                                              modifyHeaders().remove("X-Frame-Options"),
                                                                                              modifyHeaders().remove("X-XSS-Protection")))
                   .build()
                   .delete()
                   .uri(url, submissionId)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                   .exchange()
                   .expectAll(
                     rsc -> rsc.expectStatus().isNoContent(),
                     rsc -> rsc.expectBody().isEmpty())
                   .expectBody(Void.class)
                   .consumeWith(document("submission-delete",
                                         pathParameters(parameterWithName("id").description("Submission ID"))));
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