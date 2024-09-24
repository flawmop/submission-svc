package com.insilicosoft.portal.svc.submission.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.insilicosoft.portal.svc.submission.persistence.entity.Message;
import com.insilicosoft.portal.svc.submission.persistence.entity.Submission;
import com.insilicosoft.portal.svc.submission.persistence.repository.SubmissionRepository;

/**
 * Submission service implementation.
 */
@Service
public class SubmissionServiceImpl implements SubmissionService {

  private final SubmissionRepository submissionRepository;

  public SubmissionServiceImpl(final SubmissionRepository submissionRepository) {
    this.submissionRepository = submissionRepository;
  }

  
  @Override
  public void rejectOnFileProcessing(long submissionId, Message problem) {
    Submission submission = submissionRepository.getReferenceById(submissionId);
    submission.rejectWithProblem(problem);
    submissionRepository.save(submission);
  }


  @Override
  public void rejectOnInvalidInput(final long submissionId, Map<String, Set<Message>> problems) {
    Submission submission = submissionRepository.getReferenceById(submissionId);
    submission.rejectWithProblems(problems);
    submissionRepository.save(submission);
  }

  @Override
  public long submit() {
    final Submission saved = submissionRepository.save(new Submission());
    return saved.getEntityId();
  }

}