package com.insilicosoft.portal.svc.rip.service;

import org.springframework.web.multipart.MultipartFile;

public interface InputProcessorService {

  public void process(MultipartFile file);

}