package com.example.clothingstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "product_view_history")
@Getter
@Setter
@NoArgsConstructor
public class ProductViewHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "viewed_at", nullable = false)
  private Instant viewedAt;

  public ProductViewHistory(User user, Product product) {
    this.user = user;
    this.product = product;
    this.viewedAt = Instant.now();
  }
}
