package com.example.clothingstore.domain;

import com.example.clothingstore.utils.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Email cannot be blank")
  private String email;

  @NotBlank(message = "Password cannot be blank")
  private String password;

  @ManyToOne
  @JoinColumn(name = "role_id")
  private Role role;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String refreshToken;

  @NotNull
  @Column(nullable = false)
  private boolean activated = false;

  @Size(max = 50)
  @Column(name = "activation_key", length = 50)
  @JsonIgnore
  private String activationKey;

  @Size(max = 50)
  @Column(name = "reset_key", length = 50)
  @JsonIgnore
  private String resetKey;

  @Column(name = "reset_date")
  private Instant resetDate = null;

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