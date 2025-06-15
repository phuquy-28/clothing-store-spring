package com.example.clothingstore.entity;

import com.example.clothingstore.enumeration.InventoryChangeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "inventory_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistory extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_variant_id")
  private ProductVariant productVariant;

  @Column(nullable = false)
  private Integer changeInQuantity;

  @Column(nullable = false)
  private Integer quantityAfterChange;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InventoryChangeType typeOfChange;

  @Column(nullable = false)
  private Instant timestamp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private Order order;

  private String notes;
}
