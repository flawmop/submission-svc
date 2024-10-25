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
   * Reject the {@link Submission} due to problems.
   * <p>
   * The structure of {@code problems} is keyed on simulation identifier (preference for
   * using optional client-defined identifier) , e.g. :
   * <pre>
   *   {
   *     &lt;client simulation id | submission id(.&lt;sim 1&gt;)&gt; : [ &lt;Message&gt;, &ltMessage2&gt; ],
   *     &lt;client simulation id | submission id(.&lt;sim 2&gt;)&gt; : [ &lt;Message&gt; ]
   *   }
   * </pre>
   * @param submissionId Affected submission identifier.
   * @param problems Collection of problems encountered.
   */
  void reject(long submissionId, Map<String, Set<Message>> problems);

  /**
   * Retrieve the {@link Submission} identified by the {@literal submissionId}.
   * 
   * @param submissionId Submission identifier.
   * @return Submission
   * @throws EntityNotAccessibleException If identified Submission not accessible.
   */
  Submission retrieve(long submissionId) throws EntityNotAccessibleException;

}