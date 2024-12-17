package com.insilicosoft.portal.svc.submission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.insilicosoft.portal.svc.submission.exception.EntityNotAccessibleException;
import com.insilicosoft.portal.svc.submission.persistence.entity.Message;
import com.insilicosoft.portal.svc.submission.persistence.entity.Submission;
import com.insilicosoft.portal.svc.submission.persistence.repository.SimulationRepository;
import com.insilicosoft.portal.svc.submission.persistence.repository.SubmissionRepository;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

  private static final Long dummySubmissionId = 999l;

  private SubmissionServiceImpl submissionService;

  @Mock
  private SimulationRepository mockSimulationRepository;
  @Mock
  private Submission mockSubmission;
  @Mock
  private SubmissionRepository mockSubmissionRepository;

  @BeforeEach
  void beforeEach() {
    this.submissionService = new SubmissionServiceImpl(mockSubmissionRepository,
                                                       mockSimulationRepository);
  }

  @DisplayName("Test Create operation")
  @Test
  void testCreate() {
    when(mockSubmissionRepository.save(any(Submission.class))).thenReturn(mockSubmission);
    final Submission created = submissionService.create();

    assertThat(created).isEqualTo(mockSubmission);
  }

  @DisplayName("Test Delete operation")
  @Nested
  class Delete {
    @DisplayName("Exception on entity not accessible")
    @Test
    void exceptionThrowOnNotAccessible() throws EntityNotAccessibleException {
      final EntityNotAccessibleException e = assertThrows(EntityNotAccessibleException.class, () -> {
        submissionService.delete(dummySubmissionId);
      });
      assertThat(e.getMessage()).isEqualTo("Submission with identifier '" + dummySubmissionId + "' was not found");
    }

    @DisplayName("Success on Entity deletion")
    @Test
    void successOnDeletion() throws EntityNotAccessibleException {
      when(mockSubmissionRepository.findById(dummySubmissionId)).thenReturn(Optional.of(mockSubmission));
      doNothing().when(mockSimulationRepository).deleteAllBySubmissionId(dummySubmissionId);
      doNothing().when(mockSubmissionRepository).delete(mockSubmission);

      submissionService.delete(dummySubmissionId);
    }
  }

  @DisplayName("Test Reject operation")
  @Test
  void testReject() {
    var dummyProblems = new HashMap<String, Set<Message>>();
    when(mockSubmissionRepository.getReferenceById(dummySubmissionId)).thenReturn(mockSubmission);
    doNothing().when(mockSubmission).rejectWithProblems(dummyProblems);
    when(mockSubmissionRepository.save(any(Submission.class))).thenReturn(mockSubmission);

    submissionService.reject(dummySubmissionId, dummyProblems);
  }

  @DisplayName("Test Retrieve operation")
  @Nested
  class Retrieve {
    @DisplayName("Exception on entity not accessible")
    @Test
    void testRetrieve() throws EntityNotAccessibleException {
      final EntityNotAccessibleException e = assertThrows(EntityNotAccessibleException.class, () -> {
        submissionService.retrieve(dummySubmissionId);
      });
      assertThat(e.getMessage()).isEqualTo("Submission with identifier '" + dummySubmissionId + "' was not found");
    }

    @DisplayName("Success on Entity retrieval")
    @Test
    void successOnDeletion() throws EntityNotAccessibleException {
      when(mockSubmissionRepository.findById(dummySubmissionId)).thenReturn(Optional.of(mockSubmission));

      final Submission retrieved = submissionService.retrieve(dummySubmissionId);

      assertThat(retrieved).isEqualTo(mockSubmission);
    }
  }
}