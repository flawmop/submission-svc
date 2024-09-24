package com.insilicosoft.portal.svc.submission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import com.insilicosoft.portal.svc.submission.exception.FileProcessingException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Simulation;
import com.insilicosoft.portal.svc.submission.persistence.repository.SimulationRepository;

@ExtendWith(MockitoExtension.class)
public class InputProcessorServiceTest {

  private InputProcessorService inputProcessorService;
  private final long submissionEntityId = 1l;

  @Mock
  private Simulation mockSimulation;
  @Mock
  private SimulationRepository mockSimulationRepository;
  @Mock
  private StreamBridge mockStreamBridge;
  @Mock
  private SubmissionService mockSubmissionService;

  @BeforeEach
  void setUp() {
    this.inputProcessorService = new InputProcessorServiceImpl(mockSimulationRepository,
                                                               mockStreamBridge,
                                                               mockSubmissionService);
  }

  @DisplayName("Fail if content is not valid.")
  @Test
  void testFailIfInvalidFileContent() {
    // Application-defined exception message
    final byte[] file1 = new byte[0];
    FileProcessingException e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.process(submissionEntityId, file1);
    });
    assertThat(e.getMessage()).isEqualTo("Content must be a JSON object");

    // Library-defined exception message 
    final byte[] file2 = "{".getBytes();
    e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.process(submissionEntityId, file2);
    });
    assertThat(e.getMessage()).startsWith("Unexpected end-of-input");

    final byte[] file3 = "{}".getBytes();
    e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.process(submissionEntityId, file3);
    });
    assertThat(e.getMessage()).isEqualTo("Could not generate any simulations");
  }

  @DisplayName("Success if content is valid.")
  @Test
  void testSuccessIfValidFileContent() throws FileProcessingException {
    // No simulations generated!
  }

}