package com.insilicosoft.portal.svc.submission.persistence.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.insilicosoft.portal.svc.submission.event.SimulationCreate;
import com.insilicosoft.portal.svc.submission.value.MessageLevel;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

/**
 * Simulation entity.
 *
 * @author geoff
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "simulation")
public class Simulation {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = IDENTITY)
  private Long entityId;

  @Column(nullable = false, updatable = false)
  private long submissionId;

  // Optional. How the client/user identifies the simulation
  @Column(name = "clientId")
  private String clientId;

  @Column(nullable = false, updatable = false)
  private int modelId;

  @Column(nullable = false, updatable = false)
  private BigDecimal pacingFrequency;

  @Column(nullable = false, updatable = false)
  private BigDecimal pacingMaxTime;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "simulation_plasmapoints",
                   foreignKey = @ForeignKey(name = "simulation_fk"),
                   joinColumns = @JoinColumn(name = "entityId"))
  @Column(nullable = false)
  // TODO : Find a way to prevent updating this collection
  private List<BigDecimal> plasmaPoints = new ArrayList<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "simulation_message",
                   foreignKey = @ForeignKey(name = "simulation_fk"),
                   joinColumns = @JoinColumn(name = "entityId"))
  @Column(nullable = false)
  // TODO : Find a way to prevent updating this collection
  private Set<Message> messages = new HashSet<>();

  // See JPA auditing
  @LastModifiedDate
  Instant lastModifiedDate;

  // See JPA auditing
  @LastModifiedBy
  String lastModifiedBy;

  // Optimistic locking concurrency control
  @Version
  private Long lockVersion;

  // Default constructor.
  Simulation() {}

  /**
   * Initialising <b>and {@code verify()}ing</b> constructor.
   * 
   * @param submissionId Submission identifier.
   * @param modelId Model identifier.
   * @param pacingFrequency Pacing frequency (Hz)
   * @param pacingMaxTime Pacing max time (Mins)
   * @param plasmaPoints Plasma points / Concentrations (μM).
   */
  public Simulation(final long submissionId, final int modelId, final BigDecimal pacingFrequency,
                    final BigDecimal pacingMaxTime, final List<BigDecimal> plasmaPoints) {
    this.submissionId = submissionId;
    this.modelId = modelId;
    this.pacingFrequency = pacingFrequency;
    this.pacingMaxTime = pacingMaxTime;
    this.plasmaPoints = plasmaPoints;

    verify();
  }

  // 

  /**
   * Retrieve a {@link SimulationCreate} event record for this {@code Simulation}.
   *  
   * @return Equivalent {@code SimulationCreate}.
   */
  public SimulationCreate toCreate() {
    return new SimulationCreate(entityId, modelId, pacingFrequency, pacingMaxTime, plasmaPoints);
  }

  public Set<Message> getMessages(final MessageLevel minLevel) {
    verify();
    Set<Message> messages = new HashSet<>();
    for (MessageLevel level: MessageLevel.values()) {
      if (level.getOrder() >= minLevel.getOrder())
        for (Message message : this.messages)
          if (message.getLevel().getOrder() >= level.getOrder())
            messages.add(message);
    }
    return messages;
  }

  private void verify() {
    messages.clear();

    if (modelId <= 0)
      mergeMessage(MessageLevel.ERROR, "Model id has invalid value of 0 (zero) or less");

    if (pacingFrequency == null) {
      mergeMessage(MessageLevel.ERROR, "Pacing Frequency not defined");
    } else {
      if (pacingFrequency.compareTo(BigDecimal.ZERO) <= 0)
        mergeMessage(MessageLevel.ERROR, "Pacing Frequency has invalid value of 0 (zero) or less");
    }

    if (pacingMaxTime == null) {
      mergeMessage(MessageLevel.ERROR, "Pacing Max. Time not defined");
    } else {
      if (pacingMaxTime.compareTo(BigDecimal.ZERO) <= 0) 
        mergeMessage(MessageLevel.ERROR, "Pacing Max. Time has invalid value of 0 (zero) or less");
    }

    for (BigDecimal plasmaPoint : plasmaPoints) {
      if (plasmaPoint == null) {
        mergeMessage(MessageLevel.WARN, "Null plasma concentration recorded (and ignored)");
        continue;
      }
      if (plasmaPoint.compareTo(BigDecimal.ZERO) < 0)
        mergeMessage(MessageLevel.ERROR, "Negative value plasma concentration encountered");
    }
  }

  private void mergeMessage(MessageLevel level, String message) {
    messages.add(new Message(level, message));
    //messages.merge(level, Set.of(message),
    //               (l1, l2) -> Stream.concat(l1.stream(), Stream.of(message)).collect(Collectors.toSet()));
  }

  // Getters/Setters

  /**
   * Retrieve the client's identifier of the simulation.
   * 
   * @return The client-defined identifier the simulation (or empty {@code Optional} if not defined).
   */
  public Optional<String> getClientId() {
    return Optional.ofNullable(clientId);
  }

  /**
   * Retrieve the entity identifier.
   * 
   * @return Entity identifier (or empty {@code Optional} if not yet persisted).
   */
  public Optional<Long> getEntityId() {
    return Optional.ofNullable(entityId);
  }

  // Boilerplate implementations

  @Override
  public String toString() {
    return "Simulation [entityId=" + entityId + ", submissionId=" + submissionId + ", modelId=" + modelId
        + ", pacingFrequency=" + pacingFrequency + ", pacingMaxTime=" + pacingMaxTime + ", plasmaPoints=" + plasmaPoints
        + ", messages=" + messages + ", lastModifiedDate=" + lastModifiedDate + ", lastModifiedBy=" + lastModifiedBy
        + ", lockVersion=" + lockVersion + "]";
  }

}