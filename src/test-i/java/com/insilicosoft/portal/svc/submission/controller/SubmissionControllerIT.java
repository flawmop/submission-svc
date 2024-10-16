package com.insilicosoft.portal.svc.submission.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/" + String.valueOf(submissionId);

      when(mockSubmissionService.retrieve(submissionId))
          .thenThrow(new EntityNotAccessibleException("Entity", "1"));

      mockMvc.perform(get(url).with(jwt().authorities(userRole)))
             .andDo(print())
             .andExpect(status().isNotFound())
             .andExpect(content().contentType(textWithCharset))
             .andExpect(content().string("Entity with identifier '1' was not found"));

      verify(mockSubmissionService).retrieve(submissionId);
    }

    @DisplayName("Success on Submission found")
    @Test
    void successOnSubmissionFound() throws Exception {
      var submissionId = 1l;
      var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/" + String.valueOf(submissionId);

      when(mockSubmissionService.retrieve(submissionId)).thenReturn(new Submission());

      mockMvc.perform(get(url).with(jwt().authorities(userRole)))
             .andDo(print())
             .andExpect(status().isOk())
             .andExpect(content().contentType(applicationJson))
             .andExpect(jsonPath("$.entityId").isEmpty())
             .andExpect(jsonPath("$.state").value(State.CREATED.toString()));

      verify(mockSubmissionService).retrieve(submissionId);
    }
  }

}
