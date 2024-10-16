package com.insilicosoft.portal.svc.submission.service;

import java.util.Map;
import java.util.Set;

import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Message;
import com.insilicosoft.portal.svc.submission.persistence.entity.Submission;

/**
 * User {@link Submission} service.
 * 
 * @author geoff
 */
public interface SubmissionService {

  /**
   * Creates a new {@link Submission}.
   * 
   * @return Submission.
   */
  Submission create();

  /**
   * Delete the {@link Submission} (and all linked entities!).
   * 
   * @param submissionId Submission identifier.
   * @throws EntityNotAccessibleException If identified Submission not accessible.
   */
  void delete(long submissionId) throws EntityNotAccessibleException;

  /**
   * Reject the {@link Submission} due to file processing problems.
   * 
   * @param submissionId Affected submission identifier.
   * @param problem Problem encountered.
   */
  void rejectOnFileProcessing(long submissionId, Message problem);

  /**
   * Reject the {@link Submission} due to input validation problems.
   * 
   * @param submissionId Affected submission identifier.
   * @param problems Collection of problems encountered.
   */
  void rejectOnInvalidInput(long submissionId, Map<String, Set<Message>> problems);

  /**
   * Retrieve the {@link Submission} identified by the {@literal submissionId}.
   * 
   * @param submissionId Submission identifier.
   * @return Submission
   * @throws EntityNotAccessibleException If identified Submission not accessible.
   */
  Submission retrieve(long submissionId) throws EntityNotAccessibleException;

}