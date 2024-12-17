package com.insilicosoft.portal.svc.submission.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.insilicosoft.portal.svc.submission.SubmissionIdentifiers;
import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.service.InputProcessorService;
import com.insilicosoft.portal.svc.submission.service.SubmissionService;

@ExtendWith(MockitoExtension.class)
public class SubmissionControllerIT2 {

  @Mock
  private InputProcessorService mockInputProcessorService;
  @Mock
  private SubmissionService mockSubmissionService;

  private WebTestClient client;

  @BeforeEach
  void beforeEach() {
    client = WebTestClient.bindToController(new SubmissionController(mockInputProcessorService,
                                                                     mockSubmissionService)).build();
  }

  @Test
  void test() throws EntityNotAccessibleException {
    var submissionId = 1l;
    var url = SubmissionIdentifiers.REQUEST_MAPPING_SUBMISSION + "/" + String.valueOf(submissionId);

    //when(mockSubmissionService.retrieve(submissionId))
    //    .thenThrow(new EntityNotAccessibleException("Entity", "1"));

    client.get().uri(url)
          .exchange()
          .expectStatus().is2xxSuccessful();

    //verifyNoInteractions(mockInputProcessorService, mockSubmissionService);
  }
}