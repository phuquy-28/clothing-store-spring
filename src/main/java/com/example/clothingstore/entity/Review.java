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

@Entity
@Table(name = "reviews")
@SQLDelete(sql = "UPDATE reviews SET is_deleted = 1 WHERE id = ?")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Review extends SoftDeleteEntity {

  private String description;

  private Double rating;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @OneToOne
  @JoinColumn(name = "line_item_id")
  private LineItem lineItem;
}
