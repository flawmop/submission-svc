package com.insilicosoft.portal.svc.rip;

/**
 * File processing exception.
 * 
 * @author geoff
 */
public class FileProcessingException extends Exception {

  private static final long serialVersionUID = - 8135834798094531867L ;

  /**
   * Initialising constructor.
   * 
   * @param message
   */
  public FileProcessingException(final String message) {
    super(message);
  }

}