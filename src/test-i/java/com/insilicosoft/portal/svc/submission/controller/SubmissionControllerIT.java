package com.insilicosoft.portal.svc.submission.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.insilicosoft.portal.svc.submission.SubmissionIdentifiers;
import com.insilicosoft.portal.svc.submission.config.SecurityConfig;
import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.persistence.State;
import com.insilicosoft.portal.svc.submission.persistence.entity.Submission;
import com.insilicosoft.portal.svc.submission.service.InputProcessorService;
import com.insilicosoft.portal.svc.submission.service.SubmissionService;

import jakarta.servlet.ServletException;

@WebMvcTest(SubmissionController.class)
@Import(SecurityConfig.class)
public class SubmissionControllerIT {

  private static final MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
  private static final MediaType textWithCharset = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);
  private static final GrantedAuthority userRole = new SimpleGrantedAuthority("ROLE_".concat(SubmissionIdentifiers.ROLE_USER));

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private InputProcessorService mockInputProcessorService;
  @MockBean
  private JwtDecoder mockJwtDecoder;
  @MockBean
  private SubmissionService mockSubmissionService;

  @DisplayName("Test GET method(s)")
  @Nested
  class GetMethods {

    @DisplayName("Fail on Submission not found")
    @Test
    void failOnSubmissionNotFound() throws Exception {
      var submissionId = 1l;
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/{id}";

      when(mockSubmissionService.retrieve(submissionId))
          .thenThrow(new EntityNotAccessibleException("Entity", "1"));

      mockMvc.perform(get(url, String.valueOf(submissionId)).with(jwt().authorities(userRole)))
             .andDo(print())
             .andExpectAll(
               status().isNotFound(),
               content().contentType(textWithCharset),
               content().string("Entity with/using identifier '1' was not found"));

      verify(mockSubmissionService).retrieve(submissionId);
    }

    @DisplayName("Success on Submission found")
    @Test
    void successOnSubmissionFound() throws Exception {
      var submissionId = 1l;
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/{id}";

      when(mockSubmissionService.retrieve(submissionId)).thenReturn(new Submission());

      mockMvc.perform(get(url, String.valueOf(submissionId)).with(jwt().authorities(userRole)))
             .andDo(print())
             .andExpectAll(
               status().isOk(),
               content().contentType(applicationJson),
               jsonPath("$.submissionId").isEmpty(),
               jsonPath("$.state").value(State.CREATED.toString()));

      verify(mockSubmissionService).retrieve(submissionId);
    }
  }

  @DisplayName("Test POST method(s)")
  @Nested
  class PostMethods {
    @DisplayName("Fail on illegal state exception because new submission object returned from mock is transient")
    @Test
    void successOnSubmissionCreation() throws Exception {
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION.concat(SubmissionIdentifiers.REQUEST_MAPPING_SIMULATION);
      final byte[] fileByteArray = "{}".getBytes();
      MockMultipartFile mockFile = new MockMultipartFile(SubmissionIdentifiers.PARAM_NAME_SIMULATION_FILE,
                                                         "request.json", applicationJson.toString(),
                                                         fileByteArray);

      // Returning a transient entity (which won't have an @Id property assigned!)
      when(mockSubmissionService.create()).thenReturn(new Submission());

      final ServletException e = assertThrows(ServletException.class, () -> {
        mockMvc.perform(multipart(url).file(mockFile).with(jwt().authorities(userRole)));
      });
      assertThat(e.getCause() instanceof IllegalStateException);
      assertThat(e.getCause().getMessage().equals("A Submission object has been created yet its @Id has not been assigned"));
    }

  }

}