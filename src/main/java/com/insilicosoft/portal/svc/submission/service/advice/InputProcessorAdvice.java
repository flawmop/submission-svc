package com.insilicosoft.portal.svc.submission.service.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.insilicosoft.portal.svc.submission.exception.FileProcessingException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Message;
import com.insilicosoft.portal.svc.submission.service.SubmissionService;
import com.insilicosoft.portal.svc.submission.value.MessageLevel;

/**
 * Advice to handle some of the exceptions thrown from the input processing service.
 */
@Aspect
@Component
public class InputProcessorAdvice {

  private SubmissionService submissionService;

  /**
   * Initialising constructor.
   * 
   * @param submissionService Submission service.
   */
  InputProcessorAdvice(final SubmissionService submissionService) {
    this.submissionService = submissionService;
  }

  /**
   * Take appropriate action if there's been a {@link FileProcessingException} during the
   * processing of the submission.
   * 
   * @param joinPoint 
   * @param fpe File processing exception caught.
   * @throws FileProcessingException Rethrow of original exception.
   */
  @AfterThrowing(pointcut = "execution(* com.insilicosoft.portal.svc.submission.service.InputProcessorService.process(..))",
                 throwing = "fpe")
  public void handleFileProcessingException(JoinPoint joinPoint, FileProcessingException fpe)
                                            throws FileProcessingException {
    final long submissionId = Long.valueOf(joinPoint.getArgs()[0].toString());
    submissionService.rejectOnFileProcessing(submissionId, new Message(MessageLevel.WARN,
                                                                       fpe.getMessage()));
    throw new FileProcessingException(fpe.getMessage());
  }

}