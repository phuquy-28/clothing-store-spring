package com.example.clothingstore.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.clothingstore.constant.AppConstant;
import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.OrderStatusHistory;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.entity.InventoryHistory;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentStatus;
import com.example.clothingstore.enumeration.InventoryChangeType;
import com.example.clothingstore.exception.BadRequestException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.OrderRepository;
import com.example.clothingstore.repository.ProductVariantRepository;
import com.example.clothingstore.repository.InventoryHistoryRepository;
import com.example.clothingstore.service.OderCancellationService;
import com.example.clothingstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OderCancallationServiceImpl implements OderCancellationService {

  private final Logger log = LoggerFactory.getLogger(OderCancallationServiceImpl.class);

  private final OrderRepository orderRepository;

  private final ProductVariantRepository productVariantRepository;

  private final InventoryHistoryRepository inventoryHistoryRepository;

  @Override
  public void cancelOrderAndReturnStock(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    if (!canCancelOrder(order)) {
      throw new BadRequestException(ErrorMessage.ORDER_CANNOT_BE_CANCELLED);
    }

    OrderStatus previousStatus = order.getStatus();

    order.setStatus(OrderStatus.CANCELLED);
    order.setPaymentStatus(PaymentStatus.FAILED);
    order.setCancelReason(AppConstant.ORDER_CANCEL_REASON);

    // Lưu lịch sử trạng thái
    OrderStatusHistory statusHistory = new OrderStatusHistory();
    statusHistory.setOrder(order);
    statusHistory.setPreviousStatus(previousStatus);
    statusHistory.setNewStatus(OrderStatus.CANCELLED);
    statusHistory.setUpdateTimestamp(Instant.now());
    statusHistory.setUpdatedBy("system");
    statusHistory.setNote(AppConstant.ORDER_CANCEL_REASON);
    order.getStatusHistories().add(statusHistory);

    order.getLineItems().forEach(lineItem -> {
      ProductVariant variant = lineItem.getProductVariant();
      if (variant != null) {
        int returnQuantity = lineItem.getQuantity().intValue();
        variant.setQuantity(variant.getQuantity() + returnQuantity);
        productVariantRepository.save(variant);

        // Ghi lại lịch sử tồn kho
        InventoryHistory history = new InventoryHistory();
        history.setProductVariant(variant);
        history.setChangeInQuantity(returnQuantity);
        history.setQuantityAfterChange(variant.getQuantity());
        history.setTypeOfChange(InventoryChangeType.ORDER_CANCEL);
        history.setTimestamp(Instant.now());
        history.setOrder(order);
        history.setNotes("Hoàn kho do hủy đơn hàng #" + order.getCode() + " bởi hệ thống");
        inventoryHistoryRepository.save(history);
      }
    });

    orderRepository.save(order);

    log.debug("Cancelled order with ID: {} and returned stock", orderId);
  }

  @Override
  public void userCancelOrder(Long orderId, String reason) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.ORDER_NOT_FOUND));

    // Verify that the current user is the owner of the order
    String currentUserEmail = SecurityUtil.getCurrentUserLogin()
        .orElseThrow(() -> new BadRequestException(ErrorMessage.USER_NOT_LOGGED_IN));

    if (!order.getUser().getEmail().equals(currentUserEmail)) {
      throw new BadRequestException(ErrorMessage.USER_NOT_AUTHORIZED);
    }

    // Check if the order can be cancelled by the user (only PENDING status)
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new BadRequestException(ErrorMessage.ORDER_CANNOT_BE_CANCELLED);
    }

    OrderStatus previousStatus = order.getStatus();

    order.setStatus(OrderStatus.CANCELLED);
    order.setPaymentStatus(PaymentStatus.FAILED);
    String cancelReason = reason != null && !reason.isBlank() ? reason : "Cancelled by user";
    order.setCancelReason(cancelReason);

    // Lưu lịch sử trạng thái
    OrderStatusHistory statusHistory = new OrderStatusHistory();
    statusHistory.setOrder(order);
    statusHistory.setPreviousStatus(previousStatus);
    statusHistory.setNewStatus(OrderStatus.CANCELLED);
    statusHistory.setUpdateTimestamp(Instant.now());
    statusHistory.setUpdatedBy(currentUserEmail);
    statusHistory.setNote(cancelReason);
    order.getStatusHistories().add(statusHistory);

    // Return stock to inventory
    order.getLineItems().forEach(lineItem -> {
      ProductVariant variant = lineItem.getProductVariant();
      if (variant != null) {
        int returnQuantity = lineItem.getQuantity().intValue();
        variant.setQuantity(variant.getQuantity() + returnQuantity);
        productVariantRepository.save(variant);

        // Ghi lại lịch sử tồn kho
        InventoryHistory history = new InventoryHistory();
        history.setProductVariant(variant);
        history.setChangeInQuantity(returnQuantity);
        history.setQuantityAfterChange(variant.getQuantity());
        history.setTypeOfChange(InventoryChangeType.ORDER_CANCEL);
        history.setTimestamp(Instant.now());
        history.setOrder(order);
        history.setNotes("Hoàn kho do hủy đơn hàng #" + order.getCode() + " bởi khách hàng");
        inventoryHistoryRepository.save(history);
      }
    });

    orderRepository.save(order);

    log.debug("User cancelled order with ID: {}", orderId);
  }

  private boolean canCancelOrder(Order order) {
    // Chỉ cho phép hủy đơn hàng ở trạng thái PENDING
    return order.getStatus() == OrderStatus.PENDING;
  }
}
