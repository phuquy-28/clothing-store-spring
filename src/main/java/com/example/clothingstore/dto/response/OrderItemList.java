package com.example.clothingstore.dto.response;

import java.time.LocalDateTime;
import com.example.clothingstore.enumeration.DeliveryMethod;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemList {
  
  private Long id;

  private String orderCode;

  private LocalDateTime orderDate;

  private String customerName;

  private Double total;

  private PaymentStatus paymentStatus;

  private OrderStatus orderStatus;

  private Long numberOfItems;

  private PaymentMethod paymentMethod;

  private DeliveryMethod deliveryMethod;
}
