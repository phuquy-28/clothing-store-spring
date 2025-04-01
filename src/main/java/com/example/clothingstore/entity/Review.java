package com.example.clothingstore.entity;

import org.hibernate.annotations.SQLDelete;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Index;

@Entity
@Table(name = "reviews",
    indexes = {@Index(name = "idx_review_product_rating", columnList = "product_id,rating"),
        @Index(name = "idx_review_is_deleted", columnList = "isDeleted")})
@SQLDelete(sql = "UPDATE reviews SET is_deleted = 1 WHERE id = ?")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Review extends SoftDeleteEntity {

  private String description;

  private Double rating;

  private boolean published = false;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @OneToOne
  @JoinColumn(name = "line_item_id")
  private LineItem lineItem;

  private Long pointsEarned = 0L;

  @OneToOne(mappedBy = "review")
  private PointHistory pointHistory;
}
