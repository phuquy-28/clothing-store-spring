package com.example.clothingstore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "line_items")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LineItem extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "product_variant_id")
  private ProductVariant productVariant;

  private Long quantity;

  private Double unitPrice;

  private Double discountAmount;

  private Double totalPrice;

  @OneToOne(mappedBy = "lineItem")
  private Review review;
}
