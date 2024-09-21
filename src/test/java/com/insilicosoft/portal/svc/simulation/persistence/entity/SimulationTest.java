package com.insilicosoft.portal.svc.simulation.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.insilicosoft.portal.svc.simulation.value.MessageLevel;

public class SimulationTest {

  @Test
  void test() {
    List<BigDecimal> pps = new ArrayList<BigDecimal>();
    pps.add(BigDecimal.ZERO);
    pps.add(null);
    pps.add(BigDecimal.ONE);
    Simulation simulation = new Simulation(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, pps);
    assertThat(simulation.getMessages(MessageLevel.WARN)).isNotEmpty();
  }

}