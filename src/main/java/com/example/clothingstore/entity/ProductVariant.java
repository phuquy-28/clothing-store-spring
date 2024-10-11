package com.example.clothingstore.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.Size;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
@Entity
@Table(name = "product_variants")
@SQLDelete(sql = "UPDATE product_variants SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "product_id", referencedColumnName = "id")
  private Product product;

  @Enumerated(EnumType.STRING)
  private Color color;

  @Enumerated(EnumType.STRING)
  private Size size;

  private Integer quantity;

  private Double differencePrice;

  @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductImage> images;

  private boolean isDeleted = false;

  private Instant deletedAt;

  private String deletedBy;
}
