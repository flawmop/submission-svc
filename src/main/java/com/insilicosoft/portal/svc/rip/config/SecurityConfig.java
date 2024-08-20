package com.insilicosoft.portal.svc.rip.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.insilicosoft.portal.svc.rip.RipIdentifiers;

/**
 * Security configuration.
 *
 * @author geoff
 */
@Configuration
public class SecurityConfig {

  private String actuatorUsername;
  private String actuatorPassword;

  /**
   * Initialising constructor.
   * 
   * @param actuatorUsername Actuator endpoint username.
   * @param actuatorPassword Actuator endpoint password.
   */
  public SecurityConfig(@Value("${com.insilicosoft.actuator.username}") String actuatorUsername,
                        @Value("${com.insilicosoft.actuator.password}") String actuatorPassword) {
    this.actuatorUsername = actuatorUsername;
    this.actuatorPassword = actuatorPassword;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // Note: We've got MVC in classpath, so mvcMatchers (not antMatchers) are in effect
    http.authorizeHttpRequests((authz) -> authz.requestMatchers(EndpointRequest.to(InfoEndpoint.class)).authenticated())
        .httpBasic(withDefaults());
    return http.build();
  }

  // Bypass security completely
  @Bean
  WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(RipIdentifiers.REQUEST_MAPPING_RUN.concat("/**"));
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user = User.withUsername(actuatorUsername)
                           .password(passwordEncoder.encode(actuatorPassword)).build();
    return new InMemoryUserDetailsManager(user);
  }
}