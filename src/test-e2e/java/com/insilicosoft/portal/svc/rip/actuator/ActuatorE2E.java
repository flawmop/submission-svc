package com.insilicosoft.portal.svc.rip.actuator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActuatorE2E {

  private String actuatorUsername;
  private String actuatorPassword;

  @Autowired
  private TestRestTemplate restTemplate;

  public ActuatorE2E(@Value("${com.insilicosoft.actuator.username}") String actuatorUsername,
                     @Value("${com.insilicosoft.actuator.password}") String actuatorPassword) {
    this.actuatorUsername = actuatorUsername;
    this.actuatorPassword = actuatorPassword;
  }

  public
  @Test
  void test() {
    ResponseEntity<String> response = restTemplate.getForEntity("/actuator/info", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void test2() {
    ResponseEntity<String> response = restTemplate.withBasicAuth(actuatorUsername, actuatorPassword)
                                                  .getForEntity("/actuator/info", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo("{}");
  }

}