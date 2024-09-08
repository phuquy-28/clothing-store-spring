package com.example.clothingstore.entity;

import com.example.clothingstore.util.SecurityUtil;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  @PrePersist
  public void prePersist() {
    this.createdAt = Instant.now();
    SecurityUtil.getCurrentUserLogin().ifPresent(username -> this.createdBy = username);
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
    SecurityUtil.getCurrentUserLogin().ifPresent(username -> this.updatedBy = username);
  }
}
