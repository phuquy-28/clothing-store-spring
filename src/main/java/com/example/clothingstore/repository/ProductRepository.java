package com.example.clothingstore.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.enumeration.OrderStatus;

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
          AND o.status = :orderStatus
      """)
  Long countSoldQuantityByProductId(@Param("productId") Long productId,
      @Param("orderStatus") OrderStatus orderStatus);


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
      LEFT JOIN li.order o
      WHERE p.isDeleted = false
      AND o.status = :orderStatus
      GROUP BY p
      ORDER BY soldQuantity DESC
      """)
  List<Product> findTopSellingProducts(@Param("orderStatus") OrderStatus orderStatus,
      Pageable pageable);

  /**
   * NEW METHOD: Finds top-selling products by aggregating sales data from line items. It groups by
   * product and calculates total quantity sold and net revenue within a date range. It also
   * supports searching by product name.
   */
  @Query(
      value = """
          SELECT p.id, p.name,
                 COALESCE(pi.public_url, '') as imageUrl,
                 SUM(li.quantity) as quantitySold,
                 SUM((li.unit_price * li.quantity) - (li.discount_amount * li.quantity)) as totalSales
          FROM products p
          JOIN product_variants pv ON p.id = pv.product_id
          JOIN line_items li ON pv.id = li.product_variant_id
          JOIN orders o ON li.order_id = o.id
          LEFT JOIN (SELECT product_id, public_url, ROW_NUMBER() OVER(PARTITION BY product_id ORDER BY image_order) as rn FROM product_images WHERE product_variant_id IS NULL) as pi
          ON p.id = pi.product_id AND pi.rn = 1
          WHERE o.status = 'DELIVERED'
          AND o.order_date BETWEEN :startDate AND :endDate
          AND (:search IS NULL OR p.name LIKE CONCAT('%', :search, '%'))
          GROUP BY p.id, p.name, pi.public_url
          ORDER BY totalSales DESC
          """,
      countQuery = """
          SELECT COUNT(DISTINCT p.id)
          FROM products p
          JOIN product_variants pv ON p.id = pv.product_id
          JOIN line_items li ON pv.id = li.product_variant_id
          JOIN orders o ON li.order_id = o.id
          WHERE o.status = 'DELIVERED'
          AND o.order_date BETWEEN :startDate AND :endDate
          AND (:search IS NULL OR p.name LIKE CONCAT('%', :search, '%'))
          """, nativeQuery = true)
  Page<Object[]> findTopSellingProducts(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate, @Param("search") String search, Pageable pageable);
}
