package com.insilicosoft.portal.svc.submission.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FileProcessingExceptionTest {

  @DisplayName("Test the initialising constructor")
  @Test
  void testConstructor() {
    final String message = "test exception message";
    final FileProcessingException e = new FileProcessingException(message);
    assertEquals(message, e.getMessage());
  }

}