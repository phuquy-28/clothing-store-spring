package com.example.clothingstore.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.clothingstore.entity.OrderStatusHistory;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

  List<OrderStatusHistory> findByOrderIdOrderByUpdateTimestampDesc(Long orderId);

  @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.id = :orderId ORDER BY osh.updateTimestamp DESC")
  List<OrderStatusHistory> findOrderStatusHistoriesByOrderId(@Param("orderId") Long orderId);

  @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.id = :orderId AND osh.order.user.id = :userId ORDER BY osh.updateTimestamp DESC")
  List<OrderStatusHistory> findOrderStatusHistoriesByOrderIdAndUserId(
      @Param("orderId") Long orderId, @Param("userId") Long userId);
}
