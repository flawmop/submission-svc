package com.insilicosoft.portal.svc.rip;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest
class RipSvcApplicationE2E {

  @MockBean
  private JwtDecoder jwtDecoder;

  @Test
  void contextLoads() {
    
  }

}