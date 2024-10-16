package com.insilicosoft.portal.svc.submission.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.exception.FileProcessingException;
import com.insilicosoft.portal.svc.submission.exception.InputVerificationException;

/**
 * REST controller advice.
 *
 * @author geoff
 */
@RestControllerAdvice
public class ControllerAdvice {

  /**
   * Something we're going to handle.
   * 
   * @param e Entity not accessible exception.
   * @return Response entity.
   */
  @ExceptionHandler(EntityNotAccessibleException.class)
  public ResponseEntity<String> handleEntityNotAccessible(EntityNotAccessibleException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  /**
   * Something we're going to handle.
   *
   * @param e File processing exception.
   * @return Response entity.
   */
  @ExceptionHandler(FileProcessingException.class)
  public ResponseEntity<String> handleFileProcessingProblem(FileProcessingException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  /**
   * Something we're going to handle.
   *
   * @param e Input verification exception.
   * @return Response entity.
   */
  @ExceptionHandler(InputVerificationException.class)
  public ResponseEntity<String> handleInputVerificationProblem(InputVerificationException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  /**
   * Max upload size exceeded.
   *
   * @param e Upload file size exception.
   * @return Response entity.
   */
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<String> handleMaxUploadSizeExceeded() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the limit.");
  }

  /**
   * Multipart resolution failure.
   *
   * @param e Multipart exception.
   * @return Response entity.
   */
  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<String> handleMultipartException() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .body("Error occurred during file upload - MultipartException");
  }

}