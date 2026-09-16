package com.alexswd.userservice.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class AuditedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @Column(name = "created_when", nullable = false, updatable = false)
  private Instant createdWhen;

  @Column(name = "updated_by", nullable = false, length = 100)
  private String updatedBy;

  @Column(name = "updated_when", nullable = false)
  private Instant updatedWhen;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdWhen = now;
    updatedWhen = now;
    createdBy = AuditContext.currentUser();
    updatedBy = createdBy;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedWhen = Instant.now();
    updatedBy = AuditContext.currentUser();
  }

  public Long getId() {
    return id;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedWhen() {
    return createdWhen;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public Instant getUpdatedWhen() {
    return updatedWhen;
  }
}
