package com.example.clothingstore.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentStatus;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  Optional<Product> findByName(String name);

  List<Product> findByNameAndIsDeletedFalse(String name);

  Optional<Product> findBySlug(String slug);

  Long countByIsDeletedFalse();

  @Query("""
          SELECT COALESCE(SUM(li.quantity), 0)
          FROM LineItem li
          JOIN li.productVariant pv
          JOIN li.order o
          WHERE pv.product.id = :productId
          AND o.paymentStatus = :paymentStatus
      """)
  Long countSoldQuantityByProductId(@Param("productId") Long productId,
      @Param("paymentStatus") PaymentStatus paymentStatus);


  @Query("""
      SELECT COALESCE(SUM(li.quantity), 0L)
      FROM LineItem li
      WHERE li.productVariant.id = :variantId
      AND li.order.status IN (com.example.clothingstore.enumeration.OrderStatus.DELIVERED,
                               com.example.clothingstore.enumeration.OrderStatus.SHIPPING,
                               com.example.clothingstore.enumeration.OrderStatus.PROCESSING)
      """)
  Long countSoldQuantityByVariantId(@Param("variantId") Long variantId);

  @Query("""
      SELECT DISTINCT p FROM Product p
      LEFT JOIN FETCH p.variants v
      LEFT JOIN FETCH p.category
      WHERE p.isDeleted = false
      """)
  List<Product> findAllWithVariants();

  @Query("""
      SELECT p, COALESCE(SUM(li.quantity), 0) as soldQuantity
      FROM Product p
      LEFT JOIN p.variants pv
      LEFT JOIN LineItem li ON li.productVariant = pv
      LEFT JOIN li.order o ON o.status = :orderStatus
      WHERE p.isDeleted = false
      GROUP BY p
      ORDER BY soldQuantity DESC
      """)
  List<Product> findTopSellingProducts(@Param("orderStatus") OrderStatus orderStatus,
      Pageable pageable);
}
