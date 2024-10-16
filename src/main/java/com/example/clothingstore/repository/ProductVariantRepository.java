package com.example.clothingstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.clothingstore.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
  
}
