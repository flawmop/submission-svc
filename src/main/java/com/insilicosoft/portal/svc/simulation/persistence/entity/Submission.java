package com.insilicosoft.portal.svc.simulation.persistence.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.insilicosoft.portal.svc.simulation.persistence.State;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * User submission entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "submission")
public class Submission {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = IDENTITY)
  private Long entityId;

  @Column(name = "state", nullable = false)
  @Enumerated(value = EnumType.STRING)
  private State state;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "submission_message", joinColumns = @JoinColumn(name = "entityId"))
  private Set<Message> messages = new HashSet<>();

  // See JPA auditing
  @CreatedDate
  Instant createdDate;

  // See JPA auditing
  @CreatedBy
  String createdBy;

  // Default constructor.

  /**
   * Initialising constructor.
   * <p>
   * Assigns the state to {@link State.CREATED}.
   */
  public Submission() {
    this.state = State.CREATED;
  }

  //

  /**
   * Reject the submission and populate with the problem as justification.
   * 
   * @param problem Problem encountered.
   */
  public void rejectWithProblem(final Message problem) {
    this.state = State.REJECTED;
    this.messages.clear();
    this.messages.add(problem);
  }

  /**
   * Reject the submission and populate with the problems as justification.
   * 
   * @param problems Problems encountered.
   */
  public void rejectWithProblems(Map<String, Set<Message>> problems) {
    this.state = State.REJECTED;
    this.messages.clear();
    for (Map.Entry<String, Set<Message>> mapEntry : problems.entrySet()) {
      String id = mapEntry.getKey();
      for (Message message : mapEntry.getValue())
        this.messages.add(new Message(message.getLevel(), id.concat(" : ").concat(message.getMessage())));
    }
  }

  // Getters/Setters

  /**
   * Retrieve the entity identifier.
   * 
   * @return Entity identifier (or {@code null} if not yet persisted).
   */
  public Long getEntityId() {
    return entityId;
  }

  // Boilerplate implementations

  @Override
  public String toString() {
    return "Submission [entityId=" + entityId + ", state=" + state + ", createdDate=" + createdDate + ", createdBy="
        + createdBy + "]";
  }

}