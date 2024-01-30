package com.insilicosoft.portal.ripsvr.event;

import java.util.List;
import java.util.Objects;

public record SimulationMessage(int modelId, Float pacingFrequency, Float pacingMaxTime,
                                List<Float> plasmaPoints) {

  public SimulationMessage {
    Objects.requireNonNull(pacingFrequency, "pacingFrequency is required");
    Objects.requireNonNull(pacingMaxTime, "pacingMaxTime is required");
    Objects.requireNonNull(plasmaPoints, "plasmaPoints is required");
  }

}