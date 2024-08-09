package com.insilicosoft.portal.svc.rip.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.insilicosoft.portal.svc.rip.service.InputProcessorService;

@Controller
@RequestMapping("/run")
public class FileAsyncUploadController {

  private final InputProcessorService inputProcessorService;

  public FileAsyncUploadController(InputProcessorService inputProcessorService) {
    this.inputProcessorService = inputProcessorService;
  }

  @GetMapping()
  public CompletableFuture<ResponseEntity<String>> get() {
    return CompletableFuture.completedFuture(ResponseEntity.ok("All good from FileAsyncUploadController!!"));
  }

  @PostMapping("/uploadAsync")
  public CompletableFuture<ResponseEntity<String>> handleFileUpload(final @RequestParam("file")
                                                                          MultipartFile file)
                                                                    throws IOException {
    if (file == null) {
      return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("No files submitted"));
    } else {
      inputProcessorService.process(file);
      return CompletableFuture.completedFuture(ResponseEntity.ok(file.getOriginalFilename()));
    }
  }

}