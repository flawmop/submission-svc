package com.insilicosoft.portal.svc.submission.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Simulation creation event record.
 */
public record SimulationCreate(long simulationId, int modelId, BigDecimal pacingFrequency,
                               BigDecimal pacingMaxTime, List<BigDecimal> plasmaPoints) {

  public SimulationCreate {
    Objects.requireNonNull(pacingFrequency, "pacingFrequency is required");
    Objects.requireNonNull(pacingMaxTime, "pacingMaxTime is required");
    Objects.requireNonNull(plasmaPoints, "plasmaPoints is required");
  }

}