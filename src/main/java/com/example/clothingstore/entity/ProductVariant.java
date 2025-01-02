package com.example.clothingstore.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.ColumnDefault;

import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.Size;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;
import jakarta.persistence.Index;

@Entity
@Table(name = "product_variants",
    indexes = {@Index(name = "idx_variant_price_diff", columnList = "differencePrice"),
        @Index(name = "idx_variant_product_price", columnList = "product_id,differencePrice"),
        @Index(name = "idx_variant_is_deleted", columnList = "isDeleted")})
@SQLDelete(sql = "UPDATE product_variants SET is_deleted = 1 WHERE id = ? AND version = ?")
@Getter
@Setter
@ToString(exclude = {"images"})
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends SoftDeleteEntity {

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

  @Version
  @ColumnDefault("0")
  private Long version;
}
