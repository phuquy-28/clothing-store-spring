package com.example.clothingstore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "product_embeddings")
@Getter
@Setter
@NoArgsConstructor
public class ProductEmbedding {

  @Id
  @Column(name = "product_id")
  private Long productId;

  @Lob
  @Column(name = "vector", nullable = false, columnDefinition = "LONGTEXT")
  private String vector; // Store vector as JSON string

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
