package com.example.clothingstore.entity;

import org.hibernate.annotations.SQLDelete;
import com.example.clothingstore.enumeration.Color;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;
import jakarta.persistence.Index;

@Entity
@Table(name = "products",
    indexes = {@Index(name = "idx_product_featured", columnList = "isFeatured"),
        @Index(name = "idx_product_created_at", columnList = "createdAt"),
        @Index(name = "idx_product_price", columnList = "price"),
        @Index(name = "idx_product_is_deleted", columnList = "isDeleted")})
@SQLDelete(sql = "UPDATE products SET is_deleted = 1 WHERE id = ?")
@Getter
@Setter
@ToString(exclude = {"images", "variants", "reviews", "promotions"})
@NoArgsConstructor
@AllArgsConstructor
public class Product extends SoftDeleteEntity {

  private String name;

  @Column(columnDefinition = "MEDIUMTEXT")
  private String description;

  private Double price;

  private String slug;

  private boolean isFeatured;

  @Enumerated(EnumType.STRING)
  private Color colorDefault;

  @ManyToOne
  @JoinColumn(name = "category_id", referencedColumnName = "id")
  private Category category;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("image_order ASC")
  private List<ProductImage> images;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductVariant> variants;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Review> reviews;

  @ManyToMany(mappedBy = "products")
  private List<Promotion> promotions;
}
