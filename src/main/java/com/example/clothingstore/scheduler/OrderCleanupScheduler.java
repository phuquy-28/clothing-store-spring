package com.example.clothingstore.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.clothingstore.entity.Order;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.service.OderCancellationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCleanupScheduler {

  private final Logger log = LoggerFactory.getLogger(OrderCleanupScheduler.class);
  
  private final OrderRepository orderRepository;

  private final OderCancellationService oderCancellationService;

  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void cleanupUnpaidVnpayOrders() {
    log.info("Running unpaid VNPay orders cleanup scheduler...");

    Instant thirtyMinutesAgo = Instant.now().minus(30, ChronoUnit.MINUTES);

    List<Order> unpaidOrders =
        orderRepository.findByPaymentMethodAndPaymentStatusAndStatusAndOrderDateBefore(
            PaymentMethod.VNPAY, PaymentStatus.PENDING, OrderStatus.PENDING, thirtyMinutesAgo);

    log.info("Found {} unpaid VNPay orders to cancel", unpaidOrders.size());

    for (Order order : unpaidOrders) {
      try {
        oderCancellationService.cancelOrderAndReturnStock(order.getId());
        log.debug("Successfully cancelled order {} and returned stock", order.getCode());
      } catch (Exception e) {
        log.error("Error cancelling order {}: {}", order.getCode(), e.getMessage());
      }
    }

    log.info("Completed unpaid VNPay orders cleanup");
  }
}
