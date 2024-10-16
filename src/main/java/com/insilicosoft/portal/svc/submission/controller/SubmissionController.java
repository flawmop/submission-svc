package com.insilicosoft.portal.svc.submission.controller;

import java.io.IOException ;
import java.net.URI;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart ;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.insilicosoft.portal.svc.submission.SubmissionIdentifiers;
import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.exception.FileProcessingException;
import com.insilicosoft.portal.svc.submission.exception.InputVerificationException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Submission;
import com.insilicosoft.portal.svc.submission.service.InputProcessorService;
import com.insilicosoft.portal.svc.submission.service.SubmissionService;

import io.micrometer.core.annotation.Timed;

/**
 * Submission controller
 *
 * @author geoff
 */
@RestController
@RequestMapping(SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION)
public class SubmissionController {

  private static final Logger log = LoggerFactory.getLogger(SubmissionController.class);

  private final InputProcessorService inputProcessorService;
  private final SubmissionService submissionService;

  /**
   * Initialising constructor.
   *
   * @param inputProcessorService Input processing implementation.
   * @param submissionService Submission service.
   */
  public SubmissionController(final InputProcessorService inputProcessorService,
                              final SubmissionService submissionService) {
    this.inputProcessorService = inputProcessorService;
    this.submissionService = submissionService;
  }

  /**
   * Retrieve a {@link Submission}.
   * 
   * @param submissionId Submission identifier.
   * @return Found submission.
   * @throws EntityNotAccessibleException If not found or not retrievable due to security.
   */
  @GetMapping(value = "/{id}")
  @Timed(value = "submission.get", description = "GET request")
  public Submission get(final @PathVariable(name = "id") long submissionId)
                        throws EntityNotAccessibleException {
    return submissionService.retrieve(submissionId);
  }

  /**
   * Handle the file uploading from a simulation {@code POST} request.
   *
   * @param file File being uploaded.
   * @return Response entity.
   * @throws FileProcessingException If problems processing file.
   * @throws InputVerificationException If supplied input is invalid.
   */
  @PostMapping(value = SubmissionIdentifiers.REQUEST_MAPPING_SIMULATION)
  @Timed(value = "submission.simulation.post", description = "Submit Simulation POST Multipart request")
  // 'required=false' to enable manual checking for null file
  public ResponseEntity<Void> createSimulation(final @RequestPart(required=false,
                                                                  value=SubmissionIdentifiers.PARAM_NAME_SIMULATION_FILE)
                                                     MultipartFile file)
                                               throws FileProcessingException, InputVerificationException {

    if (file == null) {
      final String message = "No Multipart file! Did you supply the parameter '" + SubmissionIdentifiers.PARAM_NAME_SIMULATION_FILE + "' in the POST request?";
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

    final Submission submission = submissionService.create();
    final Long submissionId = submission.getEntityId();

    inputProcessorService.process(submissionId, fileByteArray);

    final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                                                    .path("/{submissionId}")
                                                    .buildAndExpand(submissionId)
                                                    .toUri();

    return ResponseEntity.created(location).build();
  }

  /**
   * Delete a {@link Submission}.
   * 
   * @param id Identifier of submission to delete.
   * @throws EntityNotAccessibleException If not found or not retrievable due to security.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  @Timed(value = "submission.delete", description = "DELETE request")
  public void delete(final @PathVariable(name = "id") long id) throws EntityNotAccessibleException {
    submissionService.delete(id);
  }
}