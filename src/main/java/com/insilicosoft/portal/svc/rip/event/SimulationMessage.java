package com.insilicosoft.portal.svc.rip.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record SimulationMessage(long simulationId, int modelId, BigDecimal pacingFrequency,
                                BigDecimal pacingMaxTime, List<BigDecimal> plasmaPoints) {

  public SimulationMessage {
    Objects.requireNonNull(pacingFrequency, "pacingFrequency is required");
    Objects.requireNonNull(pacingMaxTime, "pacingMaxTime is required");
    Objects.requireNonNull(plasmaPoints, "plasmaPoints is required");
  }

}