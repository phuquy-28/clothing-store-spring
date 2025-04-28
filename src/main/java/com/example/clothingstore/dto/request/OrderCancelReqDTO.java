package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCancelReqDTO {

  @NotNull(message = "orderId.not.null")
  private Long orderId;

  @NotBlank(message = "reason.not.blank")
  private String reason;
}
