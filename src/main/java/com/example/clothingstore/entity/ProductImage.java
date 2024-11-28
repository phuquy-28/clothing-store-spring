package com.example.clothingstore.entity;

import org.hibernate.annotations.SQLDelete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "product_images")
@SQLDelete(sql = "UPDATE product_images SET is_deleted = 1 WHERE id = ?")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends SoftDeleteEntity {

  private String publicUrl;

  private String gcsUrl;

  @Column(name = "image_order")
  private Integer imageOrder;

  @ManyToOne
  @JoinColumn(name = "product_id", referencedColumnName = "id")
  private Product product;

  @ManyToOne
  @JoinColumn(name = "product_variant_id", referencedColumnName = "id")
  private ProductVariant productVariant;
}
