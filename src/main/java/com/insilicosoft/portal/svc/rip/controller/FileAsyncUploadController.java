package com.insilicosoft.portal.svc.rip.controller;

import java.io.IOException ;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart ;
import org.springframework.web.multipart.MultipartFile;

import com.insilicosoft.portal.svc.rip.exception.FileProcessingException;
import com.insilicosoft.portal.svc.rip.service.InputProcessorService;

/**
 * File upload controller
 *
 * @author geoff
 */
@Controller
@RequestMapping("/run")
public class FileAsyncUploadController {

  public static final String UPLOAD_FILE_PARAM_NAME = "mpfile";

  private static final Logger log = LoggerFactory.getLogger(FileAsyncUploadController.class);

  private final InputProcessorService inputProcessorService;

  /**
   * Initialising constructor.
   *
   * @param inputProcessorService Input processing implementation.
   */
  public FileAsyncUploadController(InputProcessorService inputProcessorService) {
    this.inputProcessorService = inputProcessorService;
  }

  // TODO Remove GET mapping in /run endpoint controller
  @GetMapping()
  public ResponseEntity<String> get() {
    return ResponseEntity.ok(inputProcessorService.get());
  }

  /**
   * Handle the file uploading from a {@code POST} request.
   *
   * @param file File being uploaded.
   * @return Response entity.
   * @throws FileProcessingException If problems processing file.
   */
  @PostMapping("/uploadAsync")
  public CompletableFuture<ResponseEntity<String>> handleFileUpload(final @RequestPart(required=false,
                                                                                       value=UPLOAD_FILE_PARAM_NAME)
                                                                          MultipartFile file)
                                                   throws FileProcessingException {

    if (file == null) {
      final String message = "The POST request must supply the parameter '" + UPLOAD_FILE_PARAM_NAME + "'";
      log.warn("~handleFileUpload() : ".concat(message));
      throw new FileProcessingException(message);
    }

    final String fileName = file.getOriginalFilename();

    // Buffer the file in-memory before handing over to async processing.
    byte[] fileByteArray = null;
    try {
      fileByteArray = file.getBytes();
    } catch (IOException e) {
      log.warn("~handleFileUpload() : IOException processing '{}' (Exception message '{}')",
               fileName, e.getMessage());
      throw new FileProcessingException(e.getMessage());
    }

    inputProcessorService.process(fileByteArray);

    return CompletableFuture.completedFuture(ResponseEntity.ok(fileName));
  }

}