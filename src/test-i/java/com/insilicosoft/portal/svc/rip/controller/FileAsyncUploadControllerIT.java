package com.insilicosoft.portal.svc.rip.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.insilicosoft.portal.svc.rip.RipIdentifiers;
import com.insilicosoft.portal.svc.rip.config.SecurityConfig;
import com.insilicosoft.portal.svc.rip.service.InputProcessorService;

@WebMvcTest(FileAsyncUploadController.class)
@Import(SecurityConfig.class)
public class FileAsyncUploadControllerIT {

  private static final MediaType textWithCharset = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);
  private static final GrantedAuthority customerRole = new SimpleGrantedAuthority("ROLE_customer");

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private InputProcessorService mockInputProcessorService;

  @MockBean
  private JwtDecoder jwtDecoder;

  @DisplayName("Test GET method(s)")
  @Nested
  class GetMethods {
    @DisplayName("Success")
    @Test
    void testGet() throws Exception {
      final String getMessage = "Message from get!";

      given(mockInputProcessorService.get()).willReturn(getMessage);

      mockMvc.perform(get(RipIdentifiers.REQUEST_MAPPING_RUN).with(jwt().authorities(customerRole)))
             //.andDo(print())
             .andExpect(status().isOk())
             .andExpect(content().contentType(textWithCharset))
             .andExpect(content().string(getMessage));

      verify(mockInputProcessorService).get();
    }
  }

}