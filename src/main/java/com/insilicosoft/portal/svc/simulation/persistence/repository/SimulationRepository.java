package com.insilicosoft.portal.svc.simulation.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insilicosoft.portal.svc.simulation.persistence.entity.Simulation;

/**
 * Repository for {@link Simulation} objects.
 * 
 * @author geoff
 */
public interface SimulationRepository extends JpaRepository<Simulation, Long> {

}