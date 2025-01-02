package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  Optional<Product> findByName(String name);

  Optional<Product> findBySlug(String slug);

  // @Query("""
  //     SELECT p
  //     FROM Product p
  //     JOIN LineItem li ON li.productVariant.product = p
  //     JOIN Order o ON li.order = o
  //     WHERE o.paymentStatus = :paymentStatus
  //     AND (:startDate IS NULL OR o.orderDate >= :startDate)
  //     GROUP BY p
  //     ORDER BY SUM(li.quantity) DESC
  //     """)
  // Page<Product> findBestSellerProducts(
  //     @Param("paymentStatus") PaymentStatus paymentStatus,
  //     @Param("startDate") Instant startDate,
  //     Pageable pageable
  // );

  // default Page<Product> findBestSellerProducts(PaymentStatus paymentStatus, Pageable pageable) {
  //     return findBestSellerProducts(paymentStatus, null, pageable);
  // }

  // @Query("""
  //     SELECT p FROM Product p
  //     WHERE p IN (
  //         SELECT DISTINCT p2 FROM Product p2
  //         LEFT JOIN p2.promotions pp
  //         WHERE pp.startDate <= :now AND pp.endDate >= :now
  //     ) OR p.category IN (
  //         SELECT DISTINCT c FROM Category c
  //         JOIN c.promotions cp
  //         WHERE cp.startDate <= :now AND cp.endDate >= :now
  //     )
  //     ORDER BY 
  //     (
  //         SELECT GREATEST(
  //             COALESCE(MAX(pp2.discountRate), 0),
  //             COALESCE(MAX(cp2.discountRate), 0)
  //         )
  //         FROM Product p3
  //         LEFT JOIN p3.promotions pp2
  //         LEFT JOIN p3.category c2
  //         LEFT JOIN c2.promotions cp2
  //         WHERE p3 = p
  //         AND (
  //             (pp2.startDate <= :now AND pp2.endDate >= :now)
  //             OR (cp2.startDate <= :now AND cp2.endDate >= :now)
  //         )
  //     ) DESC
  //     """)
  // Page<Product> findDiscountedProducts(@Param("now") Instant now, Pageable pageable);

  Long countByIsDeletedFalse();
}
