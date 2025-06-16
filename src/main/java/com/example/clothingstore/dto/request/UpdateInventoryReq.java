package com.example.clothingstore.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInventoryReq {

  @NotBlank(message = "inventory.sku.not.blank")
  private String sku;

  @NotNull(message = "inventory.quantity.not.null")
  @Min(value = 0, message = "inventory.quantity.min")
  private Long quantity;

  private String note;
}
