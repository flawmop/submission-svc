package com.insilicosoft.portal.svc.simulation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.insilicosoft.portal.svc.simulation.SimulationIdentifiers;

import dasniko.testcontainers.keycloak.KeycloakContainer;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class SimulationControllerE2E {

  private static final String message = "No Multipart file! Did you supply the parameter '" + SimulationIdentifiers.PARAM_NAME_SIMULATION_FILE + "' in the POST request?";
  private static final String postUrl = SimulationIdentifiers.REQUEST_MAPPING_SIMULATION;
  private static final HttpHeaders httpHeaders = new HttpHeaders();
  private static final String goodRequestFileName = "request_good.json";
  private static final Path goodPath = Path.of("src",  "test", "resources", "requests", goodRequestFileName);

  private static KeycloakToken bjornTokens;

  {
    httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
  }

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
    @DisplayName("Success")
    @Test
    void success() {
      webTestClient.get()
                   .uri(SimulationIdentifiers.REQUEST_MAPPING_SIMULATION)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                   .exchange()
                   .expectStatus().isOk()
                   .expectBody(String.class).value(body -> {
                     assertThat(body).isEqualTo("All good from SimulationController->InputProcessorService!!");
                   });
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
                  .expectBody(String.class).value(body -> {
                    assertThat(body).isEqualTo("Error occurred during file upload - MultipartException");
                  });
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
                  .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                  .exchange()
                  .expectStatus().isBadRequest()
                  .expectBody(String.class).value(body -> {
                    assertThat(body).isEqualTo(message);
                  });
    }

    @DisplayName("Success on a good simulations request file")
    @Test
    void success() {
      var multipartBodyBuilder = new MultipartBodyBuilder();
      multipartBodyBuilder.part(SimulationIdentifiers.PARAM_NAME_SIMULATION_FILE,
                                new FileSystemResource(goodPath));
      webTestClient.post()
                   .uri(postUrl)
                   .headers(headers -> {
                     headers.setBearerAuth(bjornTokens.accessToken);
                   })
                  .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                  .exchange()
                  .expectStatus().isOk()
                  .expectBody(String.class).value(body -> {
                    assertThat(body).isEqualTo("1");
                  });

    }
  }

  private static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
    return webClient.post()
                    .body(BodyInserters.fromFormData("grant_type", "password")
                                       .with("client_id", "polar-test")
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