package com.insilicosoft.portal.svc.rip.persistence.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
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

  // See JPA auditing
  @CreatedDate
  Instant createdDate;

  // See JPA auditing
  @LastModifiedDate
  Instant lastModifiedDate;

  // See JPA auditing
  @CreatedBy
  String createdBy;

  // See JPA auditing
  @LastModifiedBy
  String lastModifiedBy;

  @Version
  int version;

  public Simulation() {}

  // Getters/Setters

  /**
   * Retrieve the entity id.
   * 
   * @return Id of entity.
   */
  public Long getEntityId() {
    return entityId;
  }

  // Boilerplate implementations

  @Override
  public String toString() {
    return "Simulation [entityId=" + entityId + ", createdDate=" + createdDate + ", lastModifiedDate="
        + lastModifiedDate + ", createdBy=" + createdBy + ", lastModifiedBy=" + lastModifiedBy + ", version=" + version
        + "]";
  }

}