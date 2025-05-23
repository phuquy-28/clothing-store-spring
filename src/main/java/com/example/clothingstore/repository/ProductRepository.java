package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.enumeration.PaymentStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  Optional<Product> findByName(String name);

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
}
