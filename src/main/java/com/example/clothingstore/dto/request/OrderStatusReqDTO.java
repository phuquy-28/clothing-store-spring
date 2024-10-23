package com.example.clothingstore.dto.request;

import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusReqDTO {

  @NotNull(message = "orderId.not.null")
  private Long orderId;

  @EnumValue(enumClass = OrderStatus.class, message = "order.status.invalid")
  private String status;
}
