package com.example.clothingstore.entity;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Index;

@Entity
@Table(name = "promotions",
    indexes = {@Index(name = "idx_promotion_dates", columnList = "startDate,endDate"),
        @Index(name = "idx_promotion_discount", columnList = "discountRate"),
        @Index(name = "idx_promotion_dates_discount",
            columnList = "startDate,endDate,discountRate")})
@Getter
@Setter
@ToString(exclude = {"products", "categories"})
@NoArgsConstructor
@AllArgsConstructor
public class Promotion extends AbstractEntity {

  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private Double discountRate;

  private Instant startDate;

  private Instant endDate;

  private String imageUrl;

  @ManyToMany
  @JoinTable(name = "promotion_products", joinColumns = @JoinColumn(name = "promotion_id"),
      inverseJoinColumns = @JoinColumn(name = "product_id"),
      indexes = {@Index(name = "idx_promo_product", columnList = "product_id,promotion_id")})
  private List<Product> products;

  @ManyToMany
  @JoinTable(name = "promotion_categories", joinColumns = @JoinColumn(name = "promotion_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"),
      indexes = {@Index(name = "idx_promo_product", columnList = "category_id,promotion_id")})
  private List<Category> categories;
}
