package com.insilicosoft.portal.svc.submission.exception;

/**
 * Exception thrown when entity not found or not visible to user.
 */
public class EntityNotAccessibleException extends Exception {

  private static final long serialVersionUID = 6311364080916164926L;

  private static final String message = "%s with/using identifier '%s' was not found";

  /**
   * Initialising constructor.
   * 
   * @param entity Entity name.
   * @param id Identifier of entity itself, or of other identifying property.
   */
  public EntityNotAccessibleException(final String entity, final String id) {
    super(String.format(message, entity, id));
  }

}