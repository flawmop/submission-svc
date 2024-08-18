package com.insilicosoft.portal.svc.rip.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.insilicosoft.portal.svc.rip.service.InputProcessorService;

@WebMvcTest
public class FileSyncUploadControllerIT {

  final static MediaType textWithCharset = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private InputProcessorService mockInputProcessorService;

  @Test
  void testGet() throws Exception {
    final String getMessage = "Message from get!";

    given(mockInputProcessorService.get()).willReturn(getMessage);

    mockMvc.perform(get("/run"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(textWithCharset));

    verify(mockInputProcessorService).get();
  }

}