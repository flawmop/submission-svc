package com.insilicosoft.portal.svc.rip.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

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
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.insilicosoft.portal.svc.rip.RipIdentifiers;
import com.insilicosoft.portal.svc.rip.exception.FileProcessingException;
import com.insilicosoft.portal.svc.rip.service.InputProcessorService;

@ExtendWith(MockitoExtension.class)
public class FileAsyncUploadControllerTest {

  private static final String message = "The POST request must supply the parameter '" + RipIdentifiers.PARAM_NAME_SIMULATION_FILE + "'";

  private FileAsyncUploadController controller;

  @Captor
  private ArgumentCaptor<byte[]> captorBytes;

  @Mock
  private InputProcessorService mockInputProcessorService;

  @BeforeEach
  void setUp() {
    controller = new FileAsyncUploadController(mockInputProcessorService);
  }

  @DisplayName("Test GET method(s)")
  @Nested
  class GetMethods {
    @DisplayName("Success")
    @Test
    void success() {
      String message = "Test get message";

      when(mockInputProcessorService.get()).thenReturn(message);

      ResponseEntity<String> response = controller.get();

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
        controller.handleFileUpload(null);
      });
      assertThat(e.getMessage()).isEqualTo(message);
    }

    @DisplayName("Success on upload")
    @Test
    void failOn() throws FileProcessingException, InterruptedException, ExecutionException {
      var bytes = "Hello, World!".getBytes();
      var fileName = "request.json";
      MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE, 
                                                     bytes);

      doNothing().when(mockInputProcessorService).processAsync(captorBytes.capture());

      ResponseEntity<String> response = controller.handleFileUpload(file).get();

      assertThat(response.getBody()).isEqualTo(fileName);

      verify(mockInputProcessorService).processAsync(captorBytes.capture());
      assertThat(captorBytes.getValue()).isEqualTo(bytes);
    }
  }

}