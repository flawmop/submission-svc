package com.insilicosoft.portal.svc.submission.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.insilicosoft.portal.svc.submission.SubmissionIdentifiers;
import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.exception.FileProcessingException;
import com.insilicosoft.portal.svc.submission.exception.InputVerificationException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Submission;
import com.insilicosoft.portal.svc.submission.service.InputProcessorService;
import com.insilicosoft.portal.svc.submission.service.SubmissionService;

@ExtendWith(MockitoExtension.class)
public class SubmissionControllerTest {

  private static final String message = "No Multipart file! Did you supply the parameter '" + SubmissionIdentifiers.PARAM_NAME_SIMULATION_FILE + "' in the POST request?";

  private SubmissionController controller;

  @Captor
  private ArgumentCaptor<byte[]> captorBytes;
  @Captor
  private ArgumentCaptor<Long> captorLong;

  @Mock
  private InputProcessorService mockInputProcessorService;
  @Mock
  private Submission mockSubmission;
  @Mock
  private SubmissionService mockSubmissionService;

  @BeforeEach
  void setUp() {
    controller = new SubmissionController(mockInputProcessorService, mockSubmissionService);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
  }

  @DisplayName("Test GET method(s)")
  @Nested
  class GetMethods {
    @DisplayName("Success")
    @Test
    void success() throws EntityNotAccessibleException {
      var submissionId = 1l;

      when(mockSubmissionService.retrieve(submissionId)).thenReturn(null);

      var submission = controller.get(submissionId);

      assertThat(submission).isNull();
    }
  }

  @DisplayName("Test POST method(s)")
  @Nested
  class PostMethods {
    @DisplayName("Fail on null param (== bad parameter naming)")
    @Test
    void failOnBadParameterNaming() {
      final FileProcessingException e = assertThrows(FileProcessingException.class, () -> {
        controller.createSimulation(null);
      });
      assertThat(e.getMessage()).isEqualTo(message);
    }

    @DisplayName("Success on upload")
    @Test
    void successOnUpload() throws FileProcessingException, InputVerificationException,
                                  URISyntaxException {
      var bytes = "Hello, World!".getBytes();
      var fileName = "request.json";
      MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
                                                     bytes);

      var submissionEntityId = 1l;
      when(mockSubmissionService.create()).thenReturn(mockSubmission);
      when(mockSubmission.getEntityId()).thenReturn(submissionEntityId);
      doNothing().when(mockInputProcessorService).process(anyLong(), any(byte[].class));

      var response = controller.createSimulation(file);

      verify(mockInputProcessorService, only()).process(captorLong.capture(), captorBytes.capture());
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getLocation()).isEqualTo(new URI("http://localhost/1"));
      assertThat(captorLong.getValue()).isSameAs(submissionEntityId);
      assertThat(captorBytes.getValue()).isEqualTo(bytes);
    }
  }

}