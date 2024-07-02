package com.example.clothingstore.domain;

import com.example.clothingstore.utils.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "addresses")
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String addressLine;

  private String city;

  private String region;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  @OneToOne(mappedBy = "address")
  @JsonIgnore
  private Customer customer;

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
