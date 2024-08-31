package com.insilicosoft.portal.svc.rip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import com.insilicosoft.portal.svc.rip.exception.FileProcessingException;
import com.insilicosoft.portal.svc.rip.persistence.entity.Simulation;
import com.insilicosoft.portal.svc.rip.persistence.repository.SimulationRepository;

@ExtendWith(MockitoExtension.class)
public class InputProcessorServiceTest {

  private InputProcessorService inputProcessorService;

  @Mock
  private Simulation mockSimulation;
  @Mock
  private SimulationRepository mockSimulationRepository;
  @Mock
  private StreamBridge mockStreamBridge;

  @BeforeEach
  void setUp() {
    this.inputProcessorService = new InputProcessorServiceImpl(mockSimulationRepository,
                                                               mockStreamBridge);
  }

  @DisplayName("Fail if content is not valid.")
  @Test
  void testFailIfInvalidFileContent() {
    // Application-defined exception message
    final byte[] file1 = new byte[0];
    String message = "Content must be a JSON object";
    FileProcessingException e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.processAsync(file1);
    });
    assertThat(e.getMessage()).isEqualTo(message);

    // Library-defined exception message 
    final byte[] file2 = "{".getBytes();
    message = "Unexpected end-of-input";
    e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.processAsync(file2);
    });
    assertThat(e.getMessage()).startsWith(message);
  }

  @DisplayName("Success if content is valid.")
  @Test
  void testSuccessIfValidFileContent() throws FileProcessingException {

    when(mockSimulationRepository.save(any(Simulation.class)))
        .thenReturn(mockSimulation);
    // No simulations generated!

    final byte[] file = "{}".getBytes();
    inputProcessorService.processAsync(file);

  }

}