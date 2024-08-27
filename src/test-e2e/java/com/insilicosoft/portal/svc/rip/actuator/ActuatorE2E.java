package com.insilicosoft.portal.svc.rip.actuator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActuatorE2E {

  private String actuatorUsername;
  private String actuatorPassword;

  @MockBean
  private JwtDecoder jwtDecoder;

  @Autowired
  private TestRestTemplate restTemplate;

  public ActuatorE2E(@Value("${com.insilicosoft.actuator.username}") String actuatorUsername,
                     @Value("${com.insilicosoft.actuator.password}") String actuatorPassword) {
    this.actuatorUsername = actuatorUsername;
    this.actuatorPassword = actuatorPassword;
  }

  @DisplayName("Test info actuator endpoint")
  @Nested
  class InfoActuator {
    @DisplayName("Fail on unauthorized")
    @Test
    void failOnUnauthorized() {
      ResponseEntity<String> response = restTemplate.getForEntity("/actuator/info", String.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @DisplayName("Success")
    @Test
    void sucess() {
      ResponseEntity<String> response = restTemplate.withBasicAuth(actuatorUsername, actuatorPassword)
                                                    .getForEntity("/actuator/info", String.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
      // This is going to expect '{}' if run in an IDE, otherwise '{build:{...etc' at CLI!
      //assertThat(response.getBody()).isEqualTo("{}");
    }
  }

}