package com.insilicosoft.portal.svc.rip.service;

import java.util.Map;
import java.util.Set;

import com.insilicosoft.portal.svc.rip.persistence.entity.Message;
import com.insilicosoft.portal.svc.rip.persistence.entity.Submission;

/**
 * User {@link Submission} service. 
 */
public interface SubmissionService {

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
   * {@link Submission} of some kind.
   * 
   * @return Submission identifier.
   */
  long submit();

}