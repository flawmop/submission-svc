package com.insilicosoft.portal.svc.submission.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * JPA Auditing configuration.
 * <p>
 * Contains mechanism to map the JWT token claims to an identity for auditing purposes.
 * 
 * @author geoff
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

  @Bean
  AuditorAware<String> auditorAware() {
    return new AuditorAware<String>() {
      @Override
      public Optional<String> getCurrentAuditor() {
        String auditor = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
          JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
          // Use the JWT 'subject' claim as identifier for audit purposes!
          auditor = token.getName();
        }
        return Optional.ofNullable(auditor);
      }
    };
  }

}