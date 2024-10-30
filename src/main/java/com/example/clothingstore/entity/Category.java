package com.example.clothingstore.entity;

import jakarta.persistence.Entity;
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
@ToString(exclude = {"products"})
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AbstractEntity {

  private String name;

  @OneToMany(mappedBy = "category")
  private List<Product> products;
}
