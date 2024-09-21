package com.insilicosoft.portal.svc.simulation.config;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import org.springframework.beans.factory.annotation.Value ;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

/**
 * Asynchronous configuration.
 *
 * @author geoff
 */
@Configuration
// Switch on ability to run @Async methods in a background thread pool
@EnableAsync
public class AsyncConfig {

  private final int corePoolSize;
  private final int maxPoolSize;
  private final int queueCapacity;
  private final String threadNamePrefix;

  private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

  /**
   * Initialising constructor.
   * 
   * @param corePoolSize Executor core pool size, e.g. 10.
   * @param maxPoolSize Executor max pool size, e.g. 10.
   * @param queueCapacity Executor queue capacity, e.g. 500.
   * @param threadNamePrefix Executor thread name prefix.
   */
  AsyncConfig(@Value("${com.insilicosoft.executor.core-pool-size}") int corePoolSize,
              @Value("${com.insilicosoft.executor.max-pool-size}") int maxPoolSize,
              @Value("${com.insilicosoft.executor.queue-capacity}") int queueCapacity,
              @Value("${com.insilicosoft.executor.thread-name-prefix}") String threadNamePrefix) {
    this.corePoolSize = corePoolSize;
    this.maxPoolSize = maxPoolSize;
    this.queueCapacity = queueCapacity;
    this.threadNamePrefix = threadNamePrefix;
  }

  /**
   * Create a {@link DelegatingSecurityContextAsyncTaskExecutor} as we want to be passing a 
   * security {@code Authentication} object to asynchronous methods (e.g. for JPA auditing purposes).
   * 
   * @return Created executor.
   */
  @Bean
  DelegatingSecurityContextAsyncTaskExecutor taskExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix(threadNamePrefix);
    executor.initialize();

    log.debug("~taskExecutor() : Executor core pool size '{}', max pool size '{}', queue capacity '{}', thread name prefix '{}'",
              corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix);

    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
  }

}