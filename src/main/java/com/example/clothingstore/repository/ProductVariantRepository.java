package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.example.clothingstore.entity.ProductVariant;

public interface ProductVariantRepository
    extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {
  Optional<ProductVariant> findBySku(String sku);

}
