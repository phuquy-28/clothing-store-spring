package com.example.clothingstore.repository;

import com.example.clothingstore.entity.ProductEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {
  Optional<ProductEmbedding> findByProductId(Long productId);

  @Query("SELECT pe FROM ProductEmbedding pe WHERE pe.productId IN :productIds")
  List<ProductEmbedding> findAllByProductIds(@Param("productIds") Set<Long> productIds);
}
