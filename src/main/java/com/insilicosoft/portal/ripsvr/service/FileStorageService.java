package com.insilicosoft.portal.ripsvr.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  public void save(MultipartFile file);

}