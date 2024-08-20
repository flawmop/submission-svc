package com.insilicosoft.portal.svc.rip.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import com.insilicosoft.portal.svc.rip.RipIdentifiers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FileAsyncUploadControllerE2E {

  private static final String postUrl = RipIdentifiers.REQUEST_MAPPING_RUN.concat(RipIdentifiers.REQUEST_MAPPING_UPLOAD_ASYNC);
  private static final HttpHeaders httpHeaders = new HttpHeaders();
  private static final String goodRequestFileName = "request_good.json";
  private static final Path goodPath = Path.of("src",  "test", "resources", "requests", goodRequestFileName);

  {
    httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Nested
  @DisplayName("Test GET method(s)")
  class getMethods {
    @DisplayName("Success")
    @Test
    void get() {
      ResponseEntity<String> response = restTemplate.getForEntity(RipIdentifiers.REQUEST_MAPPING_RUN,
                                                                  String.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo("All good from FileAsyncUploadController->InputProcessorService!!");
    }
  }

  @Nested
  @DisplayName("Test POST method(s)")
  class postMethods {
    @DisplayName("Fail on expected request param not supplied")
    @Test
    void postFailOnBadParamName() {
      var linkedMVMap = new LinkedMultiValueMap<>();

      ResponseEntity<String> response = restTemplate.postForEntity(postUrl,
                                                                   new HttpEntity<>(linkedMVMap, httpHeaders),
                                                                   String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody()).isEqualTo("The POST request must supply the parameter '" + RipIdentifiers.PARAM_NAME_SIMULATION_FILE + "'");
    }

    @DisplayName("Success on a good simulations request file")
    @Test
    void postSuccess() {
      var linkedMVMap = new LinkedMultiValueMap<>();
      linkedMVMap.add(RipIdentifiers.PARAM_NAME_SIMULATION_FILE, new FileSystemResource(goodPath));

      ResponseEntity<String> response = restTemplate.postForEntity(postUrl,
                                                                   new HttpEntity<>(linkedMVMap, httpHeaders),
                                                                   String.class);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(goodRequestFileName);
    }
  }
}