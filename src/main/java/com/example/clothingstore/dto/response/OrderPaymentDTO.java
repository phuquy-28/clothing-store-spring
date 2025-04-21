package com.example.clothingstore.dto.response;

import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.enumeration.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderPaymentDTO {

  private Long orderId;

  private String code;

  private OrderStatus status;

  private PaymentStatus paymentStatus;

  private PaymentMethod paymentMethod;

  private String paymentUrl;

}
