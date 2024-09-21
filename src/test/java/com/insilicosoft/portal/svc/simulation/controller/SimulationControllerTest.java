package com.insilicosoft.portal.svc.simulation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.insilicosoft.portal.svc.simulation.SimulationIdentifiers;
import com.insilicosoft.portal.svc.simulation.exception.FileProcessingException;
import com.insilicosoft.portal.svc.simulation.exception.InputVerificationException;
import com.insilicosoft.portal.svc.simulation.service.InputProcessorService;
import com.insilicosoft.portal.svc.simulation.service.SubmissionService;

@ExtendWith(MockitoExtension.class)
public class SimulationControllerTest {

  private static final String message = "No Multipart file! Did you supply the parameter '" + SimulationIdentifiers.PARAM_NAME_SIMULATION_FILE + "' in the POST request?";

  private SimulationController controller;

  @Captor
  private ArgumentCaptor<byte[]> captorBytes;
  @Captor
  private ArgumentCaptor<Long> captorLong;

  @Mock
  private InputProcessorService mockInputProcessorService;
  @Mock
  private SubmissionService mockSubmissionService;

  @BeforeEach
  void setUp() {
    controller = new SimulationController(mockInputProcessorService, mockSubmissionService);
  }

  @DisplayName("Test GET method(s)")
  @Nested
  class GetMethods {
    @DisplayName("Success")
    @Test
    void success() {
      var message = "Test get message";

      when(mockInputProcessorService.get()).thenReturn(message);

      var response = controller.get();

      assertThat(response.getBody()).isEqualTo(message);
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
    void failOn() throws FileProcessingException, InputVerificationException {
      var bytes = "Hello, World!".getBytes();
      var fileName = "request.json";
      MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE,
                                                     bytes);

      var submissionEntityId = 1l;
      when(mockSubmissionService.submit()).thenReturn(submissionEntityId);
      doNothing().when(mockInputProcessorService).process(anyLong(), any(byte[].class));

      var response = controller.createSimulation(file);

      verify(mockInputProcessorService, only()).process(captorLong.capture(), captorBytes.capture());
      assertThat(response.getBody()).isEqualTo(String.valueOf(submissionEntityId));
      assertThat(captorLong.getValue()).isSameAs(submissionEntityId);
      assertThat(captorBytes.getValue()).isEqualTo(bytes);
    }
  }

}