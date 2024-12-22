package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.service.OderCancellationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OderCancallationServiceImpl implements OderCancellationService {

  private final Logger log = LoggerFactory.getLogger(OderCancallationServiceImpl.class);

  private final OrderRepository orderRepository;

  private final ProductVariantRepository productVariantRepository;

  @Override
  @Transactional
  public void cancelOrderAndReturnStock(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    if (!canCancelOrder(order)) {
      throw new BadRequestException(ErrorMessage.ORDER_CANNOT_BE_CANCELLED);
    }

    order.setStatus(OrderStatus.CANCELLED);
    order.setPaymentStatus(PaymentStatus.FAILED);
    order.setCancelReason(AppConstant.ORDER_CANCEL_REASON);

    order.getLineItems().forEach(lineItem -> {
      ProductVariant variant = lineItem.getProductVariant();
      if (variant != null) {
        variant.setQuantity(variant.getQuantity() + lineItem.getQuantity().intValue());
        productVariantRepository.save(variant);
      }
    });

    orderRepository.save(order);

    log.debug("Cancelled order with ID: {} and returned stock", orderId);
  }

  private boolean canCancelOrder(Order order) {
    // Chỉ cho phép hủy đơn hàng ở trạng thái PENDING hoặc PROCESSING
    return order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING;
  }
}
