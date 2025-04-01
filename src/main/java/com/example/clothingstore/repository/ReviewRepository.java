package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  Optional<Review> findByLineItemId(Long lineItemId);

  Page<Review> findByProductSlugAndPublishedTrue(String slug, Pageable pageable);

  Page<Review> findAll(Specification<Review> specification, Pageable pageable);

  @Query("""
        SELECT COUNT(r)
        FROM Review r
        WHERE r.product.id = :productId
        AND r.published = true
      """)
  Long countPublishedReviewsByProductId(@Param("productId") Long productId);

  @Query("""
        SELECT COALESCE(AVG(r.rating), 0)
        FROM Review r
        WHERE r.product.id = :productId
        AND r.published = true
      """)
  Double calculateAverageRatingByProductId(@Param("productId") Long productId);
}
