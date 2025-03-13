package com.example.clothingstore.dto.request;

import java.util.List;
import com.example.clothingstore.annotation.EnumValue;
import com.example.clothingstore.enumeration.DeliveryMethod;
import com.example.clothingstore.enumeration.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrderPreviewReqDTO {

  private Long shippingProfileId;

  @NotEmpty(message = "cartItemIds.not.empty")
  private List<Long> cartItemIds;

  private String note;

  @EnumValue(enumClass = PaymentMethod.class, message = "paymentMethod.invalid")
  private String paymentMethod;

  @EnumValue(enumClass = DeliveryMethod.class, message = "deliveryMethod.invalid")
  private String deliveryMethod;

  private Boolean isUsePoint = false;
}
