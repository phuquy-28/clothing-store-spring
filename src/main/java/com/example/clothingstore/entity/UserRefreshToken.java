package com.example.clothingstore.entity;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_refresh_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String refreshToken;

  @Column(name = "created_date")
  private Instant createdDate;

  @Column(name = "expiry_date")
  private Instant expiryDate;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
}
