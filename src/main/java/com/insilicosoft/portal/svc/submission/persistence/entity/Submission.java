package com.insilicosoft.portal.svc.submission.persistence.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.insilicosoft.portal.svc.submission.persistence.State;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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
  private Long submissionId;

  @Column(name = "state", nullable = false)
  @Enumerated(value = EnumType.STRING)
  private State state;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "submission_message",
                   foreignKey = @ForeignKey(name = "simulation_fk"),
                   joinColumns = @JoinColumn(name = "submissionId"))
  @Column(nullable = false)
  private Set<Message> messages = new HashSet<>();

  // See JPA auditing
  @CreatedDate
  Instant createdDate;

  // See JPA auditing
  @CreatedBy
  String createdBy;

  // Optimistic locking concurrency control
  @Version
  private Long lockVersion;

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
   * Reject the submission and populate with the problems as justification.
   * <p>
   * Assigns the state to {@link State.REJECTED}
   * 
   * @param problems Problems encountered.
   */
  public void rejectWithProblems(final Map<String, Set<Message>> problems) {
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
   * Retrieve the submission identifier.
   * 
   * @return Submission identifier (or {@code null} if not yet persisted).
   */
  public Long getSubmissionId() {
    return submissionId;
  }

  /**
   * Retrieve the Submission state.
   * 
   * @return the state
   */
  public State getState() {
    return state;
  }

  // Boilerplate implementations

  @Override
  public String toString() {
    return "Submission [submissionId=" + submissionId + ", state=" + state + ", messages=" + messages + ", createdDate="
        + createdDate + ", createdBy=" + createdBy + ", lockVersion=" + lockVersion + "]";
  }

}