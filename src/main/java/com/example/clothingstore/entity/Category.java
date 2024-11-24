package com.example.clothingstore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@ToString(exclude = {"products", "promotions"})
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AbstractEntity {

  private String name;

  private String imageUrl;

  @OneToMany(mappedBy = "category")
  private List<Product> products;

  @ManyToMany(mappedBy = "categories")
  private List<Promotion> promotions;
}
