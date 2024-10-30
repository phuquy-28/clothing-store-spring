package com.example.clothingstore.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
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
import java.time.Instant;

@Entity
@Table(name = "product_images")
@SQLDelete(sql = "UPDATE product_images SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends AbstractEntity {

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

  private boolean isDeleted = false;

  private Instant deletedAt;

  private String deletedBy;
}
