package com.example.clothingstore.domain;

import com.example.clothingstore.utils.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "customers")
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "address_id")
  private Address address;

  private Instant createdAt;

  private Instant updatedAt;

  private String createdBy;

  private String updatedBy;

  @OneToOne(mappedBy = "customer")
  @JsonIgnore
  private User user;

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
