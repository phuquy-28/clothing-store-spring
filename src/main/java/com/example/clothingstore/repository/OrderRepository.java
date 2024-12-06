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

}
