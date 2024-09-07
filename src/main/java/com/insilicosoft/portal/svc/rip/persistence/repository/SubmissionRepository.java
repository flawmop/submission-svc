package com.insilicosoft.portal.svc.rip.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insilicosoft.portal.svc.rip.persistence.entity.Submission;

/**
 * Simulation(s) submission request from user.
 * <p>
 * A {@link Submission} could generate multiple {@code Simulation}s.
 */
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

}