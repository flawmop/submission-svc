package com.insilicosoft.portal.svc.submission.service;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Message;
import com.insilicosoft.portal.svc.submission.persistence.entity.Submission;
import com.insilicosoft.portal.svc.submission.persistence.repository.SimulationRepository;
import com.insilicosoft.portal.svc.submission.persistence.repository.SubmissionRepository;

import jakarta.transaction.Transactional;

/**
 * Submission service implementation.
 * 
 * @author geoff
 */
@Service
public class SubmissionServiceImpl implements SubmissionService {

  private static final Logger log = LoggerFactory.getLogger(SubmissionServiceImpl.class);

  private final SimulationRepository simulationRepository;
  private final SubmissionRepository submissionRepository;

  /**
   * Initialising constructor.
   * 
   * @param submissionRepository Submission repository..
   * @param simulationRepository Simulation repository.
   */
  public SubmissionServiceImpl(final SubmissionRepository submissionRepository,
                               final SimulationRepository simulationRepository) {
    this.submissionRepository = submissionRepository;
    this.simulationRepository = simulationRepository;
  }

  @Override
  public Submission create() {
    log.debug("~create() : Invoked.");

    return submissionRepository.save(new Submission());
  }

  @Override
  @Transactional
  public void delete(final long submissionId) throws EntityNotAccessibleException {
    log.debug("~delete() : Invoked for id {}", submissionId);

    Submission submission = submissionRepository.findById(submissionId)
                                                .orElseThrow(() -> new EntityNotAccessibleException("Submission",
                                                                                                    String.valueOf(submissionId)));
    simulationRepository.deleteAllBySubmissionId(submissionId);
    submissionRepository.delete(submission);
  }

  @Override
  public void rejectOnFileProcessing(final long submissionId, final Message problem) {
    log.debug("~rejectOnFileProcessing() : Invoked for id {}", submissionId);

    Submission submission = submissionRepository.getReferenceById(submissionId);
    submission.rejectWithProblem(problem);
    submissionRepository.save(submission);
  }

  @Override
  public void rejectOnInvalidInput(final long submissionId, final Map<String, Set<Message>> problems) {
    log.debug("~rejectOnInvalidInput() : Invoked for id {}", submissionId);

    Submission submission = submissionRepository.getReferenceById(submissionId);
    submission.rejectWithProblems(problems);
    submissionRepository.save(submission);
  }

  @Override
  public Submission retrieve(final long submissionId) throws EntityNotAccessibleException {
    log.debug("~retrieve() : Invoked for id {}", submissionId);

    return submissionRepository.findById(submissionId)
                               .orElseThrow(() -> new EntityNotAccessibleException("Submission",
                                                                                   String.valueOf(submissionId)));
  }

}