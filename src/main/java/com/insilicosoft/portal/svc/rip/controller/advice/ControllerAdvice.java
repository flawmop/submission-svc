package com.insilicosoft.portal.svc.rip.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.insilicosoft.portal.svc.rip.FileProcessingException ;

@RestControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler(FileProcessingException.class)
  public ResponseEntity<String> handleFileProcessingProblem(FileProcessingException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<String> handleMaxUploadSizeExceeded() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds the limit.");
  }

  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<String> handleMultipartException() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .body("Error occurred during file upload - MultipartException");
  }

}