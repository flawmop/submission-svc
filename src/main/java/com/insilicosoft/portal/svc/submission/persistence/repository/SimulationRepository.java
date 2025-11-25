package com.insilicosoft.portal.svc.submission.persistence.repository;

import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.insilicosoft.portal.svc.submission.persistence.entity.Simulation;

/**
 * Repository for {@link Simulation} objects.
 * 
 * @author geoff
 */
public interface SimulationRepository extends JpaRepository<Simulation, Long> {

  @Modifying
  @Query("DELETE FROM Simulation WHERE submissionId = ?1")
  void deleteAllBySubmissionId(long submissionId);

  Stream<Simulation> findAllBySubmissionId(long submissionId);
}