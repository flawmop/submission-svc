package com.insilicosoft.portal.svc.submission.service;

import static com.insilicosoft.portal.svc.submission.SubmissionIdentifiers.BINDING_NAME_SIMULATION_INPUT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import com.insilicosoft.portal.svc.submission.event.SimulationCreate;
import com.insilicosoft.portal.svc.submission.exception.FileProcessingException;
import com.insilicosoft.portal.svc.submission.exception.InputVerificationException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Simulation;
import com.insilicosoft.portal.svc.submission.persistence.repository.SimulationRepository;

@ExtendWith(MockitoExtension.class)
public class InputProcessorServiceTest {

  private InputProcessorService inputProcessorService;
  private final long submissionEntityId = 1l;

  @Captor
  private ArgumentCaptor<String> captorString;
  @Captor
  private ArgumentCaptor<SimulationCreate> captorSimulationMessage;

  @Mock
  private Simulation mockSimulation;
  @Mock
  private SimulationCreate mockSimulationCreate;
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

    final String fileContent = "{ \"%s\" : %s }";

    String section = "fish"; // bad section (expecting 'simulations')
    String value = "[]";     // good value

    final byte[] file4 = String.format(fileContent, section, value).getBytes();
    e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.process(submissionEntityId, file4);
    });
    assertThat(e.getMessage()).isEqualTo("Unrecognised document section '" + section + "', expected values are '" + Arrays.toString(FieldsSections.values()) + "'");

    section = FieldsSections.simulations.toString();       // good section
    value = "\"chips\"";                                   // bad value

    final byte[] file5 = String.format(fileContent,  section, value).getBytes();
    e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.process(submissionEntityId, file5);
    });
    assertThat(e.getMessage()).isEqualTo("'simulations' value must be an array");

    // Array expected for simulations
    value = "[]";            // expected structure for simulations, but no content

    final byte[] file6 = String.format(fileContent, section, value).getBytes();
    e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.process(submissionEntityId, file6);
    });
    assertThat(e.getMessage()).isEqualTo("Could not generate any simulations");

    final String badFileContent = "{ \"simulations\": [ { \"modelIdd\" : 1, \"pacingMaxTime\": 5, \"plasmaPoints\": [ 0, 0.3, 1, 3, 10 ], \"pacingFrequency\": 0.5 } ] }";

    final byte[] file7 = badFileContent.getBytes();
    e = assertThrows(FileProcessingException.class, () -> {
      inputProcessorService.process(submissionEntityId, file7);
    });
    assertThat(e.getMessage()).isEqualTo("Unrecognised simulation section 'modelIdd', expected values are '[modelId, pacingFrequency, pacingMaxTime, plasmaPoints]'");

  }

  @DisplayName("Fail if content is not valid.")
  @Test
  void testFailOnInputVerification() {
    final String fileContent = "{ \"simulations\": [ { \"modelId\" : 0, \"pacingMaxTime\": -5, \"plasmaPoints\": [ -10, 0.3, 1, 3, 10 ], \"pacingFrequency\": -1.5 } ] }";
    final byte[] file = fileContent.getBytes();

    final InputVerificationException e = assertThrows(InputVerificationException.class, () -> {
      inputProcessorService.process(submissionEntityId, file);
    });
    assertThat(e.getMessage()).isEqualTo("Problems encountered translating the simulation input");
  }

  @DisplayName("Success if content is valid.")
  @Test
  void testSuccessIfValidFileContent() throws FileProcessingException, InputVerificationException {
    final String fileContent = "{ \"simulations\": [ { \"modelId\" : 1, \"pacingMaxTime\": 5, \"plasmaPoints\": [ 0, 0.3, 1, 3, 10 ], \"pacingFrequency\": 0.5 } ] }";
    final byte[] file = fileContent.getBytes();

    when(mockSimulationRepository.save(any(Simulation.class))).thenReturn(mockSimulation);
    when(mockSimulation.toCreate()).thenReturn(mockSimulationCreate);
    when(mockStreamBridge.send(any(String.class), any(SimulationCreate.class))).thenReturn(true);

    inputProcessorService.process(submissionEntityId, file);

    verify(mockSimulation, only()).toCreate();  // We're not calling getMessages() on the mock!
    verify(mockStreamBridge, only()).send(captorString.capture(), captorSimulationMessage.capture());
    assertThat(captorString.getValue()).isEqualTo(BINDING_NAME_SIMULATION_INPUT);
    assertThat(captorSimulationMessage.getValue()).isEqualTo(mockSimulationCreate);
  }

}