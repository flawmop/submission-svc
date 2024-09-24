package com.insilicosoft.portal.svc.submission.exception;

/**
 * Input verification exception.
 * <p>
 * Subsequent to a successful receipt of the input, verify that it is valid. If it's not, throw
 * this exception.
 *
 * @author geoff
 */
public class InputVerificationException extends Exception {

  private static final long serialVersionUID = -5793083895981506346L;

  /**
   * Initialising constructor.
   * 
   * @param message
   */
  public InputVerificationException(final String message) {
    super(message);
  }

}