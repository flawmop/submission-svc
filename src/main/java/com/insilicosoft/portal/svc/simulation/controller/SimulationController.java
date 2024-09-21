package com.insilicosoft.portal.svc.simulation.controller;

import java.io.IOException ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart ;
import org.springframework.web.multipart.MultipartFile;

import com.insilicosoft.portal.svc.simulation.SimulationIdentifiers;
import com.insilicosoft.portal.svc.simulation.exception.FileProcessingException;
import com.insilicosoft.portal.svc.simulation.exception.InputVerificationException;
import com.insilicosoft.portal.svc.simulation.service.InputProcessorService;
import com.insilicosoft.portal.svc.simulation.service.SubmissionService;

import io.micrometer.core.annotation.Timed;

/**
 * Simulation controller
 *
 * @author geoff
 */
@Controller
@RequestMapping(SimulationIdentifiers.REQUEST_MAPPING_SIMULATION)
public class SimulationController {

  private static final Logger log = LoggerFactory.getLogger(SimulationController.class);

  private final InputProcessorService inputProcessorService;
  private final SubmissionService submissionService;

  /**
   * Initialising constructor.
   *
   * @param inputProcessorService Input processing implementation.
   * @param submissionService Submission service.
   */
  public SimulationController(final InputProcessorService inputProcessorService,
                              final SubmissionService submissionService) {
    this.inputProcessorService = inputProcessorService;
    this.submissionService = submissionService;
  }

  @GetMapping()
  @Timed(value = "simulation.get", description = "GET request")
  public ResponseEntity<String> get() {
    return ResponseEntity.ok(inputProcessorService.get());
  }

  /**
   * Handle the file uploading from a simulation {@code POST} request.
   *
   * @param file File being uploaded.
   * @return Response entity.
   * @throws FileProcessingException If problems processing file.
   */
  @PostMapping()
  @Timed(value = "simulation.post", description = "Simulation POST Multipart request")
  public ResponseEntity<String> createSimulation(final @RequestPart(required=false,
                                                                    value=SimulationIdentifiers.PARAM_NAME_SIMULATION_FILE)
                                                       MultipartFile file)
                                                 throws FileProcessingException,
                                                        InputVerificationException {

    if (file == null) {
      final String message = "No Multipart file! Did you supply the parameter '" + SimulationIdentifiers.PARAM_NAME_SIMULATION_FILE + "' in the POST request?";
      log.warn("~createSimulation() : ".concat(message));
      throw new FileProcessingException(message);
    }

    final String fileName = file.getOriginalFilename();

    // Buffer the file in-memory before handing over to async processing.
    byte[] fileByteArray = null;
    try {
      fileByteArray = file.getBytes();
    } catch (IOException e) {
      log.warn("~createSimulation() : IOException processing '{}' (Exception message '{}')",
               fileName, e.getMessage());
      throw new FileProcessingException(e.getMessage());
    }

    final long submissionId = submissionService.submit();

    inputProcessorService.process(submissionId, fileByteArray);

    return ResponseEntity.ok(String.valueOf(submissionId));
  }

}