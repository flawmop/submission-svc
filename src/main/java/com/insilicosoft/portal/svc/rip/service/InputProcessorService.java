package com.insilicosoft.portal.svc.rip.service;

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
   */
  public void process(byte[] file);

}