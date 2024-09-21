package com.insilicosoft.portal.svc.simulation.exception;

/**
 * File processing exception.
 * <p>
 * Throw this exception if there's been a problem receiving and extracting the content of the file.
 * 
 * @author geoff
 * @see InputVerificationException
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