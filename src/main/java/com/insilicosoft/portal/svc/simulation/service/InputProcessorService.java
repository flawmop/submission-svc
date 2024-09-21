package com.insilicosoft.portal.svc.simulation.service;

import com.insilicosoft.portal.svc.simulation.exception.FileProcessingException;
import com.insilicosoft.portal.svc.simulation.exception.InputVerificationException;
import com.insilicosoft.portal.svc.simulation.persistence.entity.Submission;

/**
 * Interface to input processing.
 *
 * @author geoff
 */
public interface InputProcessorService {

  // TODO Remove
  String get();

  /**
   * Process a file.
   *
   * @param submissionId {@link Submission} entity identifier.
   * @param file Non-{@code null} byte array.
   * @throws FileProcessingException If file processing problems.
   * @throws InputVerificationException If file content is not valid.
   */
  void process(long submissionId, byte[] file) throws FileProcessingException,
                                                      InputVerificationException;

  // TODO: Perform file processing asynchronously
  /*
   * Process a file asynchronously (e.g. using the method {@code @Async} notation) a byte array).
   * <p>
   * For auditing purposes, there <b>MUST</b> be an {@link Authentication} object returned by
   * {@code SecurityContextHolder.getContext().getAuthentication()} when this method runs.
   *
   * @param submissionId {@link Submission} entity identifier.
   * @param file Non-{@code null} byte array.
   */
  //void processAsync(long submissionId, byte[] file);

}