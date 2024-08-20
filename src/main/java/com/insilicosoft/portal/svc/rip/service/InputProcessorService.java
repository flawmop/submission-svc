package com.insilicosoft.portal.svc.rip.service;

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
   * Process a file asyncronously (e.g. using the method {@code @Async} notation) a byte array).
   *
   * @param file Byte array file.
   * @throws FileProcessingException If file processing problems.
   */
  public void processAsync(byte[] file) throws FileProcessingException;

}