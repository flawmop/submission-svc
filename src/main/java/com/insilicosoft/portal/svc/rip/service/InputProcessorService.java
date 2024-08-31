package com.insilicosoft.portal.svc.rip.service;

import org.springframework.security.core.Authentication;

import com.insilicosoft.portal.svc.rip.exception.FileProcessingException;

/**
 * Interface to input processing.
 *
 * @author geoff
 */
public interface InputProcessorService {

  // TODO Remove
  public String get();

  /**
   * Process a file asynchronously (e.g. using the method {@code @Async} notation) a byte array).
   * <p>
   * For auditing purposes, there <b>MUST</b> be an {@link Authentication} object returned by
   * {@code SecurityContextHolder.getContext().getAuthentication()} when this method runs.
   *
   * @param file Non-{@code null} byte array.
   * @throws FileProcessingException If file processing problems.
   */
  public void processAsync(byte[] file) throws FileProcessingException;

}