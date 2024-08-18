package com.insilicosoft.portal.svc.rip.service;

import com.insilicosoft.portal.svc.rip.FileProcessingException ;

/**
 * Interface to input processing.
 *
 * @author geoff
 */
public interface InputProcessorService {

  /**
   * Process a file (as a byte array).
   *
   * @param file Byte array file.
   * @throws FileProcessingException If file processing problems.
   */
  public void process(byte[] file) throws FileProcessingException;

}