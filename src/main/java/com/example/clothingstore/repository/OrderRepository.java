package com.example.clothingstore.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.User;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import java.time.Instant;
import java.util.List;

public interface OrderRepository
    extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

  Optional<Order> findByCode(String code);

  List<Order> findByUser(User user);

  @Query("""
      SELECT DISTINCT o FROM Order o
      LEFT JOIN FETCH o.lineItems li
      LEFT JOIN FETCH li.productVariant pv
      LEFT JOIN FETCH pv.product p
      LEFT JOIN FETCH o.user
      WHERE o.id = :orderId
      """)
  Order findOrderWithDetailsById(@Param("orderId") Long orderId);

  @Query("""
      SELECT DISTINCT pv FROM ProductVariant pv
      LEFT JOIN FETCH pv.images
      WHERE pv IN (
          SELECT li.productVariant FROM LineItem li 
          WHERE li.order.id = :orderId
      )
      """)
  List<ProductVariant> findProductVariantsWithImagesByOrderId(@Param("orderId") Long orderId);

  List<Order> findByPaymentMethodAndPaymentStatusAndStatusAndOrderDateBefore(
      PaymentMethod paymentMethod, PaymentStatus paymentStatus, OrderStatus status,
      Instant thirtyMinutesAgo);

  @Query("""
      SELECT SUM(o.finalTotal) FROM Order o 
      WHERE o.status = :status
      """)
  Long sumFinalTotalByStatus(@Param("status") OrderStatus status);

  @Query("""
      SELECT MONTH(o.orderDate) as month, SUM(o.finalTotal) as revenue 
      FROM Order o 
      WHERE YEAR(o.orderDate) = :year 
      AND o.status = :status 
      GROUP BY MONTH(o.orderDate)
      """)
  List<Object[]> findRevenueByMonth(@Param("year") Long year, @Param("status") OrderStatus status);

  @Query("""
      SELECT o FROM Order o 
      WHERE o.user.id = :userId 
      AND o.orderDate >= :startDate 
      AND o.orderDate < :endDate
      """)
  List<Order> findOrdersByUserAndDateRange(
      @Param("userId") Long userId, 
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT COUNT(o) FROM Order o 
      WHERE o.user.id = :userId 
      AND o.orderDate >= :startDate 
      AND o.orderDate < :endDate
      """)
  int countOrdersByUserAndDateRange(
      @Param("userId") Long userId, 
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT SUM(o.finalTotal) FROM Order o 
      WHERE o.user.id = :userId 
      AND o.orderDate >= :startDate 
      AND o.orderDate < :endDate
      """)
  Double sumOrderTotalByUserAndDateRange(
      @Param("userId") Long userId, 
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT o.status, COUNT(o), SUM(o.finalTotal) FROM Order o 
      WHERE o.user.id = :userId 
      AND o.orderDate >= :startDate 
      AND o.orderDate < :endDate 
      GROUP BY o.status
      """)
  List<Object[]> getOrderStatsByStatusForUserAndDateRange(
      @Param("userId") Long userId, 
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate), 
      SUM(o.finalTotal), COUNT(o) 
      FROM Order o 
      WHERE o.user.id = :userId 
      AND o.orderDate >= :startDate 
      AND o.orderDate < :endDate 
      GROUP BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate)
      ORDER BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate)
      """)
  List<Object[]> getMonthlySummaryForUser(
      @Param("userId") Long userId, 
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT o.status, SUM(o.finalTotal)
      FROM Order o 
      WHERE o.user.id = :userId 
      AND o.orderDate >= :startDate 
      AND o.orderDate < :endDate 
      GROUP BY o.status
      """)
  List<Object[]> getSpendingByStatusForUser(
      @Param("userId") Long userId, 
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT COALESCE(SUM(o.total - COALESCE(o.discount, 0.0) - COALESCE(o.pointDiscount, 0.0)), 0.0)
      FROM Order o 
      WHERE o.status = :status 
      AND o.orderDate BETWEEN :startDate AND :endDate
      """)
  Double sumFinalTotalByStatusAndOrderDateBetween(
      @Param("status") OrderStatus status,
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT COUNT(o) 
      FROM Order o 
      WHERE o.orderDate BETWEEN :startDate AND :endDate
      """)
  Long countByOrderDateBetween(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("""
      SELECT MONTH(o.orderDate) as month, 
      COALESCE(SUM(o.total - COALESCE(o.discount, 0.0) - COALESCE(o.pointDiscount, 0.0)), 0.0) as revenue 
      FROM Order o 
      WHERE YEAR(o.orderDate) = :year AND o.status = :status 
      GROUP BY MONTH(o.orderDate)
      """)
  List<Object[]> findRevenueByMonthWithoutShippingFee(@Param("year") Long year, @Param("status") OrderStatus status);

  @Query("""
      SELECT MONTH(o.orderDate) as month, COUNT(o.id) as orderCount 
      FROM Order o 
      WHERE YEAR(o.orderDate) = :year 
      GROUP BY MONTH(o.orderDate)
      """)
  List<Object[]> findOrderCountByMonth(@Param("year") Long year);
}
