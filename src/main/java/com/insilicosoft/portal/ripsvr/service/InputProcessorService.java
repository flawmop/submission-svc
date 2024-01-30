package com.insilicosoft.portal.ripsvr.service;

import org.springframework.web.multipart.MultipartFile;

public interface InputProcessorService {

  public void process(MultipartFile file);

}